package it.softfork.shijianji.models

import tech.minna.playjson.macros.jsonFlat

// TODO: Add validation
@jsonFlat case class BitcoinAddress (value: String) extends AnyVal
