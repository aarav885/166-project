DROP INDEX IF EXISTS hotel_id_num;
DROP INDEX IF EXISTS get_room_num;
CREATE INDEX hotel_id_num on Hotel USING BTREE(hotelID);
CREATE INDEX get_room_num on Rooms USING BTREE(roomNumber);