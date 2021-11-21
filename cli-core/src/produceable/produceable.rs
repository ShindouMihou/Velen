use clap::Parser;
use crate::category::Category;
use crate::command::Command;

#[derive(Parser)]
pub enum Produceable {
    COMMAND(Command),
    CATEGORY(Category),
}