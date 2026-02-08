-- Enable SMTP email sending in the database
USE gymm;

-- Update system_settings to enable SMTP
UPDATE system_settings 
SET setting_value = 'true' 
WHERE setting_key = 'smtp.enabled';

-- Verify settings
SELECT setting_key, setting_value 
FROM system_settings 
WHERE setting_key LIKE 'smtp%'
ORDER BY setting_key;
