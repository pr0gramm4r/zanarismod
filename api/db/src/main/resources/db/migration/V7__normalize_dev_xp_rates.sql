UPDATE realms
SET
    player_xp_rate_in_hundreds = 100,
    global_xp_rate_in_hundreds = 100
WHERE name = 'dev';

UPDATE characters
SET xp_rate_in_hundreds = 100
WHERE realm_id IN (
    SELECT id
    FROM realms
    WHERE name = 'dev'
);
