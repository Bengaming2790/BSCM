BSCM (Ben's S#### Config Mod)

BSCM is a lightweight, YAML configuration library for Fabric mods. It simplifies config creation, loading, and editing, so you can focus on building features instead of boilerplate.

## Features

- Annotation-based configs

Define your config structure directly in code with simple annotations.

- YAML-backed

Human-readable, clean, and widely supported configuration format.

- Automatic loading & saving No need to manually change config YAML, BSCM does it all for you.

Built-in config screen (Mod Menu support) Easily edit in-game configs with Mod Menu.

- Minimal & developer-friendly

Designed to stay out of your way while giving you full control.

## Usage

1. Create a config class

2. Annotate fields you want to expose

3. Let BSCM handle the

Example:

Gradle:

`'repositories{

maven {

name = "Modrinth"

url = "https://api.modrinth.com/maven"

dependencies {

modImplementation "maven.modrinth:bscm:1.0.0"

}

Config Class:

public class ExampleConfig {

@Comment("Enable the feature") public boolean enabled = true:

@Comment("Maximum value") public int maxValue = 10;

Main Class:

Java

import ca.techgarage.bscm.Bscm;

public void onInitialize() {

Bscm.load(ExampleConfig.class, "file name here");

BSCM will automatically:

Generate a YAML file

Load values on startup

Save changes when updated

## In-Game Configuration

If you have [**Mod Menu**] (https://modrinth.com/mod/modmenu) installed, BSCM provides a simple config GUI so users can edit settings without leaving the game.

##

Dependencies

Fabric Loader ≥ 0.15.0

(Optional) Mod Menu for in-game config UI

## Why BSCM?

Most config systems are either:

Too complex
