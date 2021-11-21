/// This is a uility method to translate a optional vector
/// string into Velen syntax. It returns an empty String if
/// it is empty.
pub fn from_vec_to_string(o: &Option<Vec<String>>, key: &str) -> String {
    match o {
        Some(t) => {
            let mut val: String = String::new();
            for v in t {
                val.push_str(&*format!("\n    {}: {}", key, v));
            }

            val
        },

        None => "".to_string()
    }
}

/// This is a uility method to translate a optional string
/// into Velen syntax. It returns an empty String if
/// it is empty.
pub fn from_option_to_string(o: &Option<String>, key: &str) -> String {
    match o {
        Some(t) => format!("\n    {}: {}", key, t).to_string(),
        None => "".to_string()
    }
}

/// This is a uility method to translate a optional int32
/// into Velen syntax. It returns an empty String if
/// it is empty.
pub fn from_opt_i32_to_string(o: &Option<i32>, key: &str) -> String {
    match o {
        Some(t) => format!("\n    {}: {}", key, t).to_string(),
        None => "".to_string()
    }
}