// ============================================================
// MinIO 图片工具：直传上传 + 私有桶预签名访问
//
// 背景：
//   MinIO 兼容 AWS S3 协议，私有桶的每一次读写都必须携带
//   AWS Signature Version 4（SigV4）签名，否则返回 403。
//   本文件不依赖任何第三方库，用浏览器自带的 crypto.subtle
//   手写签名过程，提供两个能力：
//     1. uploadImage      —— 管理端上传图片（PUT，签名放在请求头）
//     2. presignImageUrl  —— 页面展示私有图片（GET，签名拼在 URL 上，
//                            这样 <img src> 无需改请求头也能通过校验）
// ============================================================

//（MinIO 连接配置：地址 / 桶名 / 区域 / 密钥）
const MINIO_ENDPOINT = 'http://localhost:9000'
const MINIO_HOST = 'localhost:9000'
const MINIO_BUCKET = 'picture'
const MINIO_REGION = 'us-east-1'
const ACCESS_KEY = 'root'
const SECRET_KEY = '12345678'

//（字符串转字节的编码器，后面到处要用）
const encoder = new TextEncoder()

//（把 ArrayBuffer 转成十六进制字符串，SHA256 和签名结果都是字节数组）
const toHex = (buffer) =>
  Array.from(new Uint8Array(buffer))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')

//（计算 SHA256 并返回 hex 字符串；入参可以是字符串或字节）
const sha256Hex = async (data) => {
  const bytes = typeof data === 'string' ? encoder.encode(data) : data
  return toHex(await crypto.subtle.digest('SHA-256', bytes))
}

//（计算 HMAC-SHA256，返回字节数组；key 可以是字符串或上一轮的字节结果）
const hmacSha256 = async (key, data) => {
  const keyBytes = typeof key === 'string' ? encoder.encode(key) : key
  const cryptoKey = await crypto.subtle.importKey(
    'raw',
    keyBytes,
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  )
  return crypto.subtle.sign('HMAC', cryptoKey, encoder.encode(data))
}

//（生成 simple/ 目录下的唯一对象名：时间戳_随机串.扩展名，避免重名覆盖）
const buildObjectName = (file) => {
  const rawExt = (file.name.split('.').pop() || '').toLowerCase()
  const ext = rawExt.replace(/[^a-z0-9]/g, '') || 'jpg'
  return `simple/${Date.now()}_${Math.random().toString(36).slice(2, 8)}.${ext}`
}

//（从 SECRET_KEY 逐级推导当天有效的签名密钥，是 SigV4 的固定四步链式 HMAC）
const deriveSigningKey = async (dateStamp) => {
  // 第 1 级：AWS4 + 密钥 与 日期 做 HMAC
  const kDate = await hmacSha256(`AWS4${SECRET_KEY}`, dateStamp)
  // 第 2 级：上级结果 与 区域 做 HMAC
  const kRegion = await hmacSha256(kDate, MINIO_REGION)
  // 第 3 级：上级结果 与 服务名 s3 做 HMAC
  const kService = await hmacSha256(kRegion, 's3')
  // 第 4 级：上级结果 与 固定结尾 aws4_request 做 HMAC，得到最终签名密钥
  return hmacSha256(kService, 'aws4_request')
}

//（把 Date 转成 SigV4 要求的两个时间格式）
const buildAmzDates = () => {
  // toISOString 形如 2026-11-07T10:30:00.000Z
  // 去掉横杠、冒号和毫秒后得到 20261107T103000Z，即 x-amz-date 格式
  const amzDate = new Date().toISOString().replace(/[:-]|\.\d{3}/g, '')
  // 前 8 位是 yyyyMMdd，用于密钥推导和凭证范围
  return { amzDate, dateStamp: amzDate.slice(0, 8) }
}

// ------------------------------------------------------------
// 能力一：上传图片（管理端使用）
// 流程：算文件哈希 -> 组规范化请求 -> 组待签名字符串 -> 算签名
//       -> 拼 Authorization 头 -> PUT 上传 -> 返回图片地址
// ------------------------------------------------------------
export const uploadImage = async (file) => {
  const objectName = buildObjectName(file)
  const body = await file.arrayBuffer()

  // 第 1 步：对文件内容算 SHA256，SigV4 要求把它写进请求头和签名
  const payloadHash = await sha256Hex(body)

  const { amzDate, dateStamp } = buildAmzDates()
  const canonicalUri = `/${MINIO_BUCKET}/${objectName}`

  // 第 2 步：拼“规范化请求”——签名对象的固定文本格式，
  // 我们只签三个头：host、内容哈希、时间
  const canonicalHeaders =
    `host:${MINIO_HOST}\n` +
    `x-amz-content-sha256:${payloadHash}\n` +
    `x-amz-date:${amzDate}\n`
  const signedHeaders = 'host;x-amz-content-sha256;x-amz-date'
  const canonicalRequest = [
    'PUT',
    canonicalUri,
    '',
    canonicalHeaders,
    signedHeaders,
    payloadHash
  ].join('\n')

  // 第 3 步：拼“待签名字符串” = 算法名 + 时间 + 凭证范围 + 规范化请求的哈希
  const scope = `${dateStamp}/${MINIO_REGION}/s3/aws4_request`
  const stringToSign = [
    'AWS4-HMAC-SHA256',
    amzDate,
    scope,
    await sha256Hex(canonicalRequest)
  ].join('\n')

  // 第 4 步：用推导出的密钥对待签名字符串做 HMAC，得到最终签名
  const signingKey = await deriveSigningKey(dateStamp)
  const signature = toHex(await hmacSha256(signingKey, stringToSign))

  // 第 5 步：把凭证、签名的头清单、签名值拼成 Authorization 头
  const authorization =
    `AWS4-HMAC-SHA256 Credential=${ACCESS_KEY}/${scope}, ` +
    `SignedHeaders=${signedHeaders}, Signature=${signature}`

  // 第 6 步：PUT 上传；MinIO 校验签名一致才会接收文件
  const url = `${MINIO_ENDPOINT}${canonicalUri}`
  const res = await fetch(url, {
    method: 'PUT',
    headers: {
      Authorization: authorization,
      'x-amz-content-sha256': payloadHash,
      'x-amz-date': amzDate
    },
    body
  })
  if (!res.ok) {
    throw new Error(`上传失败（${res.status}）`)
  }

  // 返回存库的“原始地址”（不带签名），展示时再临时换预签名地址
  return url
}

// ------------------------------------------------------------
// 能力二：私有桶图片展示（所有页面使用）
// <img> 标签无法自定义请求头，所以改用 SigV4 的“预签名 URL”方案：
// 把算法、凭证、时间、有效期、签名全部作为查询参数拼在 URL 上，
// MinIO 拿到参数后自行验签，在有效期内任何人都能访问这张图。
// ------------------------------------------------------------

//（从原始地址里解析出对象名；不是本桶的地址（如外链）返回 null）
const parseMinioUrl = (rawUrl) => {
  try {
    const u = new URL(rawUrl)
    if (u.host !== MINIO_HOST) return null
    const parts = u.pathname.split('/').filter(Boolean)
    if (parts.length < 2 || parts[0] !== MINIO_BUCKET) return null
    return parts.slice(1).join('/')
  } catch {
    return null
  }
}

//（把一张图片的原始地址换成带时效的预签名地址，默认 24 小时有效）
export const presignImageUrl = async (rawUrl, expires = 86400) => {
  const objectName = parseMinioUrl(rawUrl)
  // 外链或空地址不处理，原样返回
  if (!objectName) return rawUrl || ''

  const { amzDate, dateStamp } = buildAmzDates()
  const scope = `${dateStamp}/${MINIO_REGION}/s3/aws4_request`
  const canonicalUri = `/${MINIO_BUCKET}/${objectName}`

  // 第 1 步：准备 5 个验签参数；注意 Credential 里的 / 也会被编码成 %2F
  const params = {
    'X-Amz-Algorithm': 'AWS4-HMAC-SHA256',
    'X-Amz-Credential': `${ACCESS_KEY}/${scope}`,
    'X-Amz-Date': amzDate,
    'X-Amz-Expires': String(expires),
    'X-Amz-SignedHeaders': 'host'
  }

  // 第 2 步：参数按 key 字典序排序并 URL 编码，拼成规范化查询串
  const canonicalQuery = Object.keys(params)
    .sort()
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
    .join('&')

  // 第 3 步：GET 的规范化请求；预签名模式下不签请求体，
  // 固定写 UNSIGNED-PAYLOAD；只签 host 一个头
  const canonicalRequest = [
    'GET',
    canonicalUri,
    canonicalQuery,
    `host:${MINIO_HOST}\n`,
    'host',
    'UNSIGNED-PAYLOAD'
  ].join('\n')

  // 第 4 步：和上传一样，组待签名字符串并用派生密钥算签名
  const stringToSign = [
    'AWS4-HMAC-SHA256',
    amzDate,
    scope,
    await sha256Hex(canonicalRequest)
  ].join('\n')
  const signingKey = await deriveSigningKey(dateStamp)
  const signature = toHex(await hmacSha256(signingKey, stringToSign))

  // 第 5 步：查询串 + 签名值 = 完整预签名地址，可直接给 <img src>
  return `${MINIO_ENDPOINT}${canonicalUri}?${canonicalQuery}&X-Amz-Signature=${signature}`
}

//（批量预签名：列表页一次换多张图时用）
export const presignImageUrls = (urls, expires) =>
  Promise.all((urls || []).map((u) => presignImageUrl(u, expires)))
