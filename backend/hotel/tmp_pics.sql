UPDATE category SET picture='["https://picsum.photos/id/1067/900/600","https://picsum.photos/id/1068/900/600"]' WHERE id='C001';
UPDATE category SET picture='["https://picsum.photos/id/1068/900/600","https://picsum.photos/id/164/900/600"]' WHERE id='C002';
UPDATE category SET picture='["https://picsum.photos/id/164/900/600","https://picsum.photos/id/1036/900/600"]' WHERE id='C003';
SELECT id, name, picture FROM category;