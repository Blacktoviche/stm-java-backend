INSERT INTO "stm_user" (ID, USERNAME, USER_PASS, FIRSTNAME, LASTNAME, ROLE_ID, EMAIL, ENABLED, last_pass_reset_date) VALUES (1, 'admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', 'admin', 1,  'admin@admin.com',true, '07-02-2018');
INSERT INTO "stm_user" (ID, USERNAME, USER_PASS, FIRSTNAME, LASTNAME, ROLE_ID, EMAIL, ENABLED, last_pass_reset_date) VALUES (2, 'user', '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'user', 'user', 2, 'user@user.com', true, '17-03-2018');

INSERT INTO "role" (ID, NAME) VALUES (1, 'ROLE_USER');
INSERT INTO "role" (ID, NAME) VALUES (2, 'ROLE_ADMIN');
