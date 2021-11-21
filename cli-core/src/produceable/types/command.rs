use std::fs::{create_dir_all, File};
use std::io::Write;
use std::path::Path;
use clap::Parser;
use handlebars::Handlebars;
use strum_macros::EnumString;
use serde_json::json;
use crate::utils::{from_opt_i32_to_string, from_option_to_string, from_vec_to_string};

#[derive(Parser)]
pub struct Command {
    #[clap(about = "The name of the command, it will also be used as the name of the file.")]
    pub name: String,
    #[clap(short, long, about = "The type of the command: Slash, Message or Hybrid.")]
    pub model: Type,
    #[clap(long, about = "The handler name of the command.")]
    pub handler: String,
    #[clap(long, about = "The description of the command.")]
    pub desc: Option<String>,
    #[clap(long, about = "The category of the command.")]
    pub category: Option<String>,
    #[clap(long, about = "The cooldown of the command, in milliseconds.")]
    pub cooldown: Option<i32>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The middlewares of the command, can have multiple occurrences or multiple values separated by spaces.")]
    pub middlewares: Option<Vec<String>>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The afterwares of the command, can have multiple occurrences or multiple values separated by spaces.")]
    pub afterwares: Option<Vec<String>>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The shortcuts or aliases of the command, can have multiple occurrences or multiple values separated by spaces.")]
    pub shortcuts: Option<Vec<String>>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The usages of the command, can have multiple occurrences or multiple values separated by spaces.")]
    pub usages: Option<Vec<String>>,
    #[clap(long, default_value = "commands", about = "The path to store the command.")]
    pub path: String
}

#[derive(Parser, EnumString)]
pub enum Type {
    #[strum(ascii_case_insensitive)]
    SLASH,
    #[strum(ascii_case_insensitive)]
    HYBRID,
    #[strum(ascii_case_insensitive)]
    MESSAGE
}

/// Produces the result that is wanted
/// by the user. This creates the file at the specified
/// destination with all the basic details filled.
pub fn produce(command: Command, handlebars: &Handlebars) {
    // Force an error if it is somehow unable to unwrap this.
    let intern = handlebars.render("velen", &json!(
        {"name": &command.name, "type": type_to_string(&command.model), "extras": produce_extras(&command)}
    )).unwrap();

    let mut path = String::new();

    if !&command.path.starts_with("./") {
        path.push_str("./");
    }

    path.push_str(&command.path.as_str());

    if !&command.path.ends_with("/") {
        path.push_str("/")
    }

    path.push_str(&command.name.as_str());
    path.push_str(".velen");

    if !Path::new(&command.path).exists() {
        create_dir_all(&command.path).unwrap();
    }

    let mut file = File::create(&path).unwrap();
    file.write_all(intern.as_ref()).unwrap();

    println!("The file was created at {}", path);
}

/// This method transforms all the options used
/// to Velen syntax and returns it back a string.
fn produce_extras(command: &Command) -> String {
    let mut val = String::new();

    val.push_str(&from_option_to_string(&command.category, "category"));
    val.push_str(&from_option_to_string(&command.desc, "desc"));
    val.push_str(&from_opt_i32_to_string(&command.cooldown, "cooldown"));
    val.push_str(&from_vec_to_string(&command.middlewares, "middleware"));
    val.push_str(&from_vec_to_string(&command.afterwares, "afterware"));
    val.push_str(&from_vec_to_string(&command.usages, "usage"));
    val.push_str(&from_vec_to_string(&command.shortcuts, "shortcut"));

    // The handler SHOULD ALWAYS be last.
    val.push_str(&*format!("\n    handler: {}", &command.handler));

    val
}

/// This is a little utility method
/// to translate Type to a String altough, to_string does exist
/// but this is still nice to have.
fn type_to_string(o: &Type) -> String {
    return match o {
        Type::MESSAGE => String::from("message"),
        Type::HYBRID => String::from("hybrid"),
        Type::SLASH => String::from("slash")
    }
}