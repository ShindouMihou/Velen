use crate::Produceable;
use clap::Parser;

#[derive(Parser)]
pub struct Make {
    #[clap(subcommand)]
    pub make: Produceable,
}