package eu.epitech.cashmanager2.cashmanager;

import eu.epitech.cashmanager2.cashmanager.model.Item;
import eu.epitech.cashmanager2.cashmanager.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.beans.JavaBean;
import java.util.*;

/**
 * Main class for this server, used to handle every connection, state and communication.
 */
@Component
public class CashHandler implements WebSocketHandler {
    private static int c = 0;
    private static FluxProcessor<Pair<Identity, String>, Pair<Identity, String>> notifier = DirectProcessor.<Pair<Identity, String>>create().serialize();
    private static FluxSink<Pair<Identity, String>> notifyList = notifier.sink();
    private static Map<Identity, Cart> Carts = new HashMap<>();
    private static Map<String, Identity> identities = new HashMap<>();

    @Autowired
    public ItemRepository itemRepository;

    private static Pair<Boolean, String> notify(String str, WebSocketSession s) {
        notifyList.next(Pair.of(Identity.getId(s), str));
        return Pair.of(false, "");
    }
    private static Pair<Boolean, String> message(String str) { return Pair.of(true, str); }
    private static Pair<Boolean, String> error(ErrorStatus st) { return Pair.of(true, st.toString()); }
    private Pair<Boolean, String> quit(WebSocketSession session) {
        session.close(CloseStatus.BAD_DATA).subscribe();
        return Pair.of(false, "");
    }

    /**
     * This method is used to handle every message sent by the client and respond accordingly
     * @param wsm original WebSocketMessage
     * @return a pair with a boolean component that tells if something must be send back to the client,
     * and if true, what to send
     */
    private Pair<Boolean, String> parse(WebSocketMessage wsm, WebSocketSession session) {
        var str = wsm.getPayloadAsText();
        var msg = new Message(str);

        if (!identities.containsKey(session.getId()) && !msg.command.equals("password"))
            return quit(session);
        if (msg.error != null)
            return error(ErrorStatus.BAD_FORMAT);

        try {
            Pair<Boolean, String> ret;
            for (Action m : commands)
                if ((ret = m.action(msg, session)) != null)
                    return ret;
            return error(ErrorStatus.UNKNOWN_COMMAND);
        } catch (Exception e) {
            return error(ErrorStatus.BAD_ARGUMENTS);
        }
    }

    /**
     * This method is used to clean up the lists
     * @param session the session to clean
     */
    private void clean(WebSocketSession session) {
        c--;
        var id = identities.getOrDefault(session.getId(), null);
        Identity.clean(session);
        if (id != null) {
            Carts.remove(id);
        }
    }

    /**
     * Main entry point that links all the Flux used for a specified connection
     * <p>
     * This method is called once per client connection
     * @param session the WebSocketSession representing the current client
     * @return nothing
     */
    @Override
    public Mono<Void> handle(WebSocketSession session)
    {
        c++;
        return session.send(
                session.receive()
                        .doOnTerminate(() -> clean(session))
                        .map(v -> this.parse(v, session))
                        .filter(Pair::getFirst)
                        .map(Pair::getSecond)
                        .mergeWith(notifier
                                .filter(v -> v.getFirst().equals(Identity.getId(session)))
                                .map(Pair::getSecond)
                        )
                        .filter(v -> identities.containsKey(session.getId()))
                        .map(session::textMessage)
                        .doOnError(e -> {
                            System.out.println("ERROR: " + e.getMessage());
                            e.printStackTrace();
                        })
        );
    }

    private interface Action {
        Pair<Boolean, String> action(Message msg, WebSocketSession session);
    }

    private final Action[] a_article = {
            (msg, s) -> {
                if (msg.parameters[0].equals("add")) {
                    if (msg.parameters.length < 2)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var name = msg.parameters[1];
                    if (name.contains(";"))
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var qty = msg.parameters.length > 2 ? msg.parameters[2] : "1";
                    try {
                        var i_qty = Integer.parseInt(qty);
                        var art = new Article(name, i_qty, itemRepository);
                        Carts.get(Identity.getId(s)).addArticle(art);
                        var ar = Carts.get(Identity.getId(s)).getArticle(art);
                        return notify("article " + ar.toString(), s);
                    } catch (NoSuchElementException e) {
                        return error(ErrorStatus.BAD_ARTICLE);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    }
                }
                return null;
            },
            (msg, s) -> {
                if (msg.parameters[0].equals("create")) {
                    if (msg.parameters.length < 3)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var name = msg.parameters[1];
                    if (name.contains(";"))
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var price = msg.parameters[2];
                    try {
                        var d_price = Double.parseDouble(price);
                        var art = new Item();
                        art.Name = name;
                        art.Price = d_price;
                        art = itemRepository.save(art);
                        return message("article OK");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    }
                }
                return null;
            },
            (msg, s) -> {
                if (msg.parameters[0].equals("remove")) {
                    if (msg.parameters.length < 2)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var name = msg.parameters[1];
                    if (name.contains(";"))
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var qty = "" + Integer.MAX_VALUE;
                    if (msg.parameters.length > 2)
                        qty = msg.parameters[2];
                    try {
                        var i_qty = Integer.parseInt(qty);
                        var art = new Article(name, i_qty, itemRepository);
                        Carts.get(Identity.getId(s)).reduceArticle(art);
                        var ar = Carts.get(Identity.getId(s)).getArticle(art);
                        return notify("article " + ar.toString(), s);
                    } catch (NoSuchElementException e) {
                        return error(ErrorStatus.BAD_ARTICLE);
                    } catch (Exception e) {
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    }
                }
                return null;
            },
            (msg, s) -> {
                if (msg.parameters[0].equals("list") ||
                    msg.parameters[0].equals("get")) {
                    return message("article " + itemRepository.findAll().stream().map((i) -> i.Name + ";" + i.Price).reduce((s1, s2) -> s1 + " " + s2).orElse(""));
                }
                return null;
            },
    };
    private final Action[] a_cart = {
            (msg, s) -> {
                if (msg.parameters[0].equals("list") ||
                    msg.parameters[0].equals("get")) {
                    return message(Carts.get(Identity.getId(s)).toString());
                }
                return null;
            },
            (msg, s) -> {
                if (msg.parameters[0].equals("clear")) {
                    Carts.get(Identity.getId(s)).clear();
                    return notify(Carts.get(Identity.getId(s)).toString(), s);
                }
                return null;
            },
    };
    private final Action[] commands = {
            (msg, s) -> {
                if (msg.command.equals("password")) {
                    if (msg.parameters.length < 1)
                        return quit(s);
                    if (identities.containsKey(s.getId()))
                        return error(ErrorStatus.UNKNOWN_COMMAND);
                    var id = new Identity(msg.parameters[0], s);
                    if (id.valid()) {
                        identities.put(s.getId(), id);
                        if (!Carts.containsKey(Identity.getId(s)))
                            Carts.put(Identity.getId(s), new Cart());
                        return message("password OK");
                    } else {
                        return quit(s);
                    }
                }
                return null;
            },
            (msg, s) -> {
                if (msg.command.equals("ping")) {
                    return message("pong");
                }
                return null;
            },
            (msg, s) -> {
                if (msg.command.equals("article")) {
                    if (msg.parameters.length < 1)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    Pair<Boolean, String> ret;
                    for (Action m : a_article)
                        if ((ret = m.action(msg, s)) != null)
                            return ret;
                    return error(ErrorStatus.UNKNOWN_ACTION);
                }
                return null;
            },
            (msg, s) -> {
                if (msg.command.equals("cart")) {
                    if (msg.parameters.length < 1)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    Pair<Boolean, String> ret;
                    for (Action m : a_cart)
                        if ((ret = m.action(msg, s)) != null)
                            return ret;
                    return error(ErrorStatus.UNKNOWN_ACTION);
                }
                return null;
            },
            (msg, s) -> {
                if (msg.command.equals("payment")) {
                    if (msg.parameters.length < 2)
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    var method = msg.parameters[0];
                    var account = msg.parameters[1];
                    if (!method.equals("card") && !method.equals("check"))
                        return error(ErrorStatus.BAD_ARGUMENTS);
                    Carts.get(Identity.getId(s)).clear();
                    return notify("payment OK", s);
                }
                return null;
            },
    };

    /**
     * Class handling the parsing of incoming messages
     */
    private static class Message {
        private String command = "";
        private String[] parameters = new String[0];
        private Exception error = null;

        Message(String str) {
            try {
                var sstr = str.split(" ");
                if (sstr.length > 0)
                    command = sstr[0];
                if (sstr.length > 1) {
                    parameters = Arrays.copyOfRange(sstr, 1, sstr.length);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                command = "";
                parameters = new String[0];
                error = e;
            }
        }
    }
}
