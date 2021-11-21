use clap::Parser;
use handlebars::Handlebars;
use produceable::Produceable;
use crate::make::Make;

#[path = "produceable/produceable.rs"] mod produceable;
#[path = "subcommands/make.rs"] mod make;
#[path = "produceable/types/command.rs"] mod command;
#[path = "produceable/types/category.rs"] mod category;
#[path = "utils/utils.rs"] mod utils;

#[derive(Parser)]
#[clap(version = "1.0", author = "Shindou Mihou <mihou@manabot.fun>")]
struct Opts {
    #[clap(subcommand)]
    subcommand: Subcommand
}

#[derive(Parser)]
enum Subcommand {
    #[clap(version = "1.0", author = "Shindou Mihou <mihou@manabot.fun")]
    Make(Make),
}

fn main() {
    let mut handlebars = Handlebars::new();

    // I would love to use /template files, but since I plan on making this cli
    // a single file without any extra files to copy and paste, yeah.
    handlebars.register_template_string("velen","&[{{name}}]: {{type}} { {{extras}}\n}").unwrap();

    let opts: Opts = Opts::parse();
    match opts.subcommand {
        Subcommand::Make(t) => {
            match t.make {
                Produceable::COMMAND(c) => {
                    command::produce(c, &handlebars);
                },
                Produceable::CATEGORY(c) => {
                    category::produce(c, &handlebars);
                }
            }
        }
    }
}
