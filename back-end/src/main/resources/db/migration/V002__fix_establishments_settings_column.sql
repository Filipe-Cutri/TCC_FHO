-- Fix the settings column type in establishments table
-- Convert from OID to TEXT type to avoid casting errors

-- If the column exists and has data, preserve it during conversion
ALTER TABLE establishments 
ALTER COLUMN settings TYPE TEXT USING CASE 
    WHEN settings IS NULL THEN NULL
    ELSE settings::TEXT
END;
