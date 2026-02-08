-- Update System Settings with Email Configuration
-- Sender: gym.zadx@gmail.com
-- App Password: nylm cudj xzks xedy (remove spaces: nylmcudjxzksxedy)

-- Delete existing email settings if any
DELETE FROM system_settings WHERE setting_key LIKE 'email.%';

-- Insert email configuration
INSERT INTO system_settings (setting_key, setting_value, description, updated_by) VALUES
('email.sender_address', 'gym.zadx@gmail.com', 'Email sender address for notifications', 1),
('email.smtp_host', 'smtp.gmail.com', 'SMTP server hostname', 1),
('email.smtp_port', '465', 'SMTP server port (SSL)', 1),
('email.smtp_username', 'gym.zadx@gmail.com', 'SMTP authentication username', 1),
('email.smtp_password', 'nylmcudjxzksxedy', 'SMTP authentication password (App Password)', 1),
('email.smtp_tls', 'true', 'Enable TLS/SSL encryption', 1);

-- Verify the settings
SELECT * FROM system_settings WHERE setting_key LIKE 'email.%';
