CREATE TABLE s_sms_send_log (
    id INTEGER,
    request_id TEXT,
    phone TEXT,
    ip TEXT,
    sms_url TEXT,
    web_site_name TEXT,
    params TEXT,
    response TEXT,
    create_at NUMERIC,
    request_duration INTEGER,
    response_status TEXT,
    CONSTRAINT s_sms_send_log_PK PRIMARY KEY (id)
);

-- s_sms_url definition

CREATE TABLE s_sms_url (
   id INTEGER,
   icon TEXT,
   website_name TEXT,
   website TEXT,
   sms_url TEXT,
   phone_param_name TEXT,
   binding_params TEXT,
   create_at NUMERIC,
   update_at NUMERIC,
   is_normal INTEGER,
   business_name TEXT,
   success_code TEXT,
   end_code TEXT,
   open_script INTEGER,
   script_name TEXT,
   script_content TEXT,
   script_path TEXT,
   request_method TEXT,
   content_type TEXT,
   headers TEXT,
   response_type TEXT,
   last_used_time NUMERIC,
   max_retry_times INTEGER,
   CONSTRAINT s_sms_url_PK PRIMARY KEY (id)
);