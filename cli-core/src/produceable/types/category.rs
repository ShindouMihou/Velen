use std::fs::{create_dir_all, File};
use std::io::Write;
use std::path::Path;
use clap::Parser;
use handlebars::Handlebars;
use serde_json::json;
use crate::utils::{from_option_to_string, from_vec_to_string};

#[derive(Parser)]
pub struct Category {
    #[clap(about = "The name of the category, it will also be used as the name of the file.")]
    pub name: String,
    #[clap(short, long, about = "The description of the category.")]
    pub desc: Option<String>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The middlewares of the category, can have multiple occurrences or multiple values separated by spaces.")]
    pub middlewares: Option<Vec<String>>,
    #[clap(long, multiple_values=true, multiple_occurrences=true, about = "The afterwares of the category, can have multiple occurrences or multiple values separated by spaces.")]
    pub afterwares: Option<Vec<String>>,
    #[clap(long, default_value = "categories", about = "The path to store the categories.")]
    pub path: String
}

/// Produces the result that is wanted
/// by the user. This creates the file at the specified
/// destination with all the basic details filled.
pub fn produce(category: Category, handlebars: &Handlebars) {
    // Force an error if it is somehow unable to unwrap this.
    let intern = handlebars.render("velen", &json!(
        {"name": &category.name, "type": "category", "extras": produce_extras(&category)}
    )).unwrap();

    let mut path = String::new();

    if !&category.path.starts_with("./") {
        path.push_str("./");
    }

    path.push_str(&category.path.as_str());

    if !&category.path.ends_with("/") {
        path.push_str("/")
    }

    path.push_str(&category.name.as_str());
    path.push_str(".vecomp");

    if !Path::new(&category.path).exists() {
        create_dir_all(&category.path).unwrap();
    }

    let mut file = File::create(&path).unwrap();
    file.write_all(intern.as_ref()).unwrap();

    println!("The file was created at {}", path);
}

/// This method transforms all the options used
/// to Velen syntax and returns it back a string.
fn produce_extras(category: &Category) -> String {
    let mut val = String::new();

    val.push_str(&from_option_to_string(&category.desc, "desc"));
    val.push_str(&from_vec_to_string(&category.middlewares, "middleware"));
    val.push_str(&from_vec_to_string(&category.afterwares, "afterware"));

    val
}