# CashManager-server
# API Protocol Specification

## Base

The connection to the API is based on websockets and can be accessed via the `/cash` route.

## Protocol

The client sends `message`s in plain text over an established connection. Each `message` is a list of strings separated by one or more spaces. The first string is called `command` while the others are grouped as `parameters`.

If the client needs to send spaces it can send an underscore ( `_` ) instead.

The client receive both response to his `command`s and `notification`s that are sent to every connected client.

The communication must start with a _`password`_ `command` with the password as first `parameter`. If the connection succeed, a _`password`_ `command` is send back with the string `OK` as `parameter`. If the connection fails, the connection is aborted server-side with a status code `BAD_DATA`.

If a `command` is not understood by the server or incorrect it will respond with a `error <status>` `command`, `<status>` being a string identifier for the current `error`.

## Commands

* `password <passwd>`:
  Sent as the very first `message` to authenticate the client.  
  → The server responds with `password OK` or closes the websocket.
* `ping`:
  Heartbeat message to keep the connection alive. Responded by `pong`.
* `article <action> [options]`:
  Used for article-based actions, with `action` being one of these values:
  * `create <name> <price>`:
    To insert a new article in the database with the specified price. The name cannot contain the semicolon (`;`) character.  
    The server responds with `article OK`.
  * `add <name> [qty]`:
    To add an article to the current cart with an optional quantity being 1 by default. The name cannot contain the semicolon (`;`) character.  
    The server broadcasts this message:  
    `article name;price;total_qty`
  * `remove <name> [qty]`:
    To remove an article from the current cart. If the quantity is not specified, removes all instances of the article.
    The server broadcasts this message:  
    `article name;price;total_qty`
  * `clear`:
    To empty the current cart and broadcast it. **Cannot be undone**.
  * `list`:
  * `get`:
    Returns the current state of the cart.  
    → The server returns the cart with this format:  
      `cart article1;price;qty article2;price;qty ...`
* `cart <action>`:
  Used to control the cart, with `action` being one of these values:
  * `list`:
  * `get`:
    Returns the current cart with this format:  
      `cart article1;price;qty article2;price;qty ...`
  * `clear`:
    To empty the current cart and broadcast it. **Cannot be undone**.
* `payment <method> <account>`:
  Used to pay a cart with the method being either a `card` or a `check`, the account being a string of the account IBAN.

## Errors

* `BAD_FORMAT`:
  If the `message` sent is not valid.
* `UNKNOWN_COMMAND`:
  If the `command` sent is not valid.
* `UNKNOWN_ACTION`:
  If the first `parameter` of a `command` is not valid for the specified `command`.
* `BAD_PARAMETERS`:
  If there is not enough parameters or if they have an unexpected format.
* `BAD_ARTICLE`:
  If the selected article does not exist in the database.

## Terms

* `command`: string used as an identifier to a specific action server-side.
* `message`: datagram used to send or receive data to/from the server.
* `notification`: command sent only by the server as a response to another client's command to inform all the clients of a server state change.
* `parameter`: string used by the command to specify more informations.
