package dk.mineclub.minecore.hooks.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.api.model.StoreRequest;
import dk.mineclub.minecore.hooks.skript.effects.EffMineCoreAcceptRequest;
import dk.mineclub.minecore.hooks.skript.effects.EffMineCoreAcceptVote;
import dk.mineclub.minecore.hooks.skript.effects.EffMineCoreAddProduct;
import dk.mineclub.minecore.hooks.skript.effects.EffMineCoreCancelRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCorePostCreateRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCorePreCreateRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreReceiveRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreReceiveVote;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequest;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequestProducts;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequestProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreMappedVoteProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreNewProductProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreNewRequestProducts;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreReceiveRequestType;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreRequestProductProperty;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import dk.mineclub.minecore.hooks.skript.sections.SecMineCoreCreateRequest;
import dk.mineclub.minecore.platform.common.event.MineCorePostCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCorePreCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveVoteEvent;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
final class SkriptRegistrations {
    private SkriptRegistrations() {}

    static void register() {
        try {
            registerEvents();
            registerSections();
            registerEffects();
            registerExpressions();
            registerClasses();
            registerEventValues();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to register MineCore Skript hooks", ex);
        }
    }

    private static void registerEvents() {
        try {
            Skript.registerEvent(
                    "MineCore Pre Create Request",
                    EvtMineCorePreCreateRequest.class,
                    MineCorePreCreateRequestEvent.class,
                    "minecore pre create request");
            Skript.registerEvent(
                    "MineCore Post Create Request",
                    EvtMineCorePostCreateRequest.class,
                    MineCorePostCreateRequestEvent.class,
                    "minecore post create request");
            Skript.registerEvent(
                    "MineCore Receive Request",
                    EvtMineCoreReceiveRequest.class,
                    MineCoreReceiveRequestEvent.class,
                    "minecore receive request");
            Skript.registerEvent(
                    "MineCore Receive Vote",
                    EvtMineCoreReceiveVote.class,
                    MineCoreReceiveVoteEvent.class,
                    "minecore receive vote");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore events: " + e.getMessage());
        }
    }

    private static void registerSections() {
        try {
            Skript.registerSection(
                    SecMineCoreCreateRequest.class,
                    "create [a] minecore request for %offlineplayer%");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore sections: " + e.getMessage());
        }
    }

    private static void registerEffects() {
        try {
            Skript.registerEffect(
                    EffMineCoreAddProduct.class,
                    "add [a] product %string% [named %-string%] [price %-number%] [quantity %-number%] [subscription[ ]days %-number%] [to [the] [minecore] request]");
            Skript.registerEffect(
                    EffMineCoreAcceptRequest.class,
                    "accept [minecore] request %object%",
                    "accept [minecore] request [with] id %string%");
            Skript.registerEffect(
                    EffMineCoreAcceptVote.class,
                    "accept [minecore] vote %object%",
                    "accept [minecore] vote [with] id %string%");
            Skript.registerEffect(
                    EffMineCoreCancelRequest.class,
                    "cancel [minecore] request %object%",
                    "cancel [minecore] request [with] id %string%");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore effects: " + e.getMessage());
        }
    }

    private static void registerClasses() {
        Classes.registerClass(
                new ClassInfo<>(StoreRequest.class, "newrequest")
                        .user("new ?requests?")
                        .name("New Request")
                        .description(
                                "An outgoing MineCore store request being built before creation.")
                        .since("1.0")
                        .parser(
                                new Parser<StoreRequest>() {
                                    @Override
                                    public boolean canParse(ParseContext context) {
                                        return false;
                                    }

                                    @Override
                                    public String toString(StoreRequest o, int flags) {
                                        return o == null ? "none" : o.toString();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreRequest o) {
                                        return "newrequest:" + o;
                                    }
                                }));
        Classes.registerClass(
                new ClassInfo<>(StoreCreatedRequest.class, "createdrequest")
                        .user("created ?requests?")
                        .name("Created Request")
                        .description("A MineCore store request that has already been created.")
                        .since("1.0")
                        .parser(
                                new Parser<StoreCreatedRequest>() {
                                    @Override
                                    public @Nullable StoreCreatedRequest parse(
                                            String s, ParseContext context) {
                                        return MineCoreSkriptApi.toRequest(s);
                                    }

                                    @Override
                                    public String toString(StoreCreatedRequest o, int flags) {
                                        return o == null ? "none" : "request " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreCreatedRequest o) {
                                        return "createdrequest:" + (o == null ? "none" : o.getId());
                                    }
                                }));
        Classes.registerClass(
                new ClassInfo<>(StoreCreatedRequest.Product.class, "requestproduct")
                        .user("request ?products?")
                        .name("Request Product")
                        .description(
                                "A product on an existing (created or received) MineCore request.")
                        .since("1.0")
                        .parser(
                                new Parser<StoreCreatedRequest.Product>() {
                                    @Override
                                    public boolean canParse(ParseContext context) {
                                        return false;
                                    }

                                    @Override
                                    public String toString(
                                            StoreCreatedRequest.Product o, int flags) {
                                        return o == null ? "none" : "product " + o.getProductId();
                                    }

                                    @Override
                                    public String toVariableNameString(
                                            StoreCreatedRequest.Product o) {
                                        return "requestproduct:"
                                                + (o == null ? "none" : o.getProductId());
                                    }
                                }));
        Classes.registerClass(
                new ClassInfo<>(StoreProduct.class, "newproduct")
                        .user("new ?products?")
                        .name("New Product")
                        .description(
                                "A product attached to an outgoing MineCore request before creation.")
                        .since("1.0")
                        .parser(
                                new Parser<StoreProduct>() {
                                    @Override
                                    public boolean canParse(
                                            ch.njol.skript.lang.ParseContext context) {
                                        return false;
                                    }

                                    @Override
                                    public String toString(StoreProduct o, int flags) {
                                        return o == null ? "none" : "new product " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreProduct o) {
                                        return "newproduct:" + (o == null ? "none" : o.getId());
                                    }
                                }));
        Classes.registerClass(
                new ClassInfo<>(MappedVote.class, "mappedvote")
                        .user("mapped ?votes?", "votes?")
                        .name("Mapped Vote")
                        .description("A MineCore vote payload received from the API.")
                        .since("1.0")
                        .parser(
                                new Parser<MappedVote>() {
                                    @Override
                                    public boolean canParse(ParseContext context) {
                                        return false;
                                    }

                                    @Override
                                    public String toString(MappedVote o, int flags) {
                                        return o == null ? "none" : "vote " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(MappedVote o) {
                                        return "mappedvote:" + (o == null ? "none" : o.getId());
                                    }
                                }));
    }

    private static void registerExpressions() {
        try {
            Skript.registerExpression(
                    ExprMineCoreReceiveRequestType.class,
                    String.class,
                    ExpressionType.SIMPLE,
                    "[the] event type",
                    "[the] event-type");
            Skript.registerExpression(
                    ExprMineCoreCreatedRequest.class,
                    StoreCreatedRequest.class,
                    ExpressionType.SIMPLE,
                    "[the] [last] [minecore] created request",
                    "[the] minecore request from [the] create section");
            Skript.registerExpression(
                    ExprMineCoreRequestProductProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of %requestproduct%",
                    "[the] name of %requestproduct%",
                    "[the] price of %requestproduct%",
                    "[the] quantity of %requestproduct%",
                    "[the] created[ ]at of %requestproduct%",
                    "[the] updated[ ]at of %requestproduct%");
            Skript.registerExpression(
                    ExprMineCoreCreatedRequestProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of %createdrequest%",
                    "[the] service of %createdrequest%",
                    "[the] server status of %createdrequest%",
                    "[the] client status of %createdrequest%",
                    "[the] created[ ]at of %createdrequest%",
                    "[the] updated[ ]at of %createdrequest%");
            Skript.registerExpression(
                    ExprMineCoreNewRequestProducts.class,
                    StoreProduct.class,
                    ExpressionType.PROPERTY,
                    "[the] [new] products of %newrequest%");
            Skript.registerExpression(
                    ExprMineCoreCreatedRequestProducts.class,
                    StoreCreatedRequest.Product.class,
                    ExpressionType.PROPERTY,
                    "[the] [new] products of %createdrequest%");
            Skript.registerExpression(
                    ExprMineCoreNewProductProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of %newproduct%",
                    "[the] name of %newproduct%",
                    "[the] price of %newproduct%",
                    "[the] quantity of %newproduct%",
                    "[the] subscription[ ]days of %newproduct%");
            Skript.registerExpression(
                    ExprMineCoreMappedVoteProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of %mappedvote%",
                    "[the] status of %mappedvote%",
                    "[the] created[ ]at of %mappedvote%",
                    "[the] updated[ ]at of %mappedvote%",
                    "[the] offlineplayer of %mappedvote%");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore expressions: " + e.getMessage());
        }
    }

    private static void registerEventValues() {
        // MineCorePreCreateRequestEvent
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                StoreRequest.class,
                new Getter<StoreRequest, MineCorePreCreateRequestEvent>() {
                    @Override
                    public StoreRequest get(MineCorePreCreateRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                EventValues.TIME_NOW);
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCorePreCreateRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCorePreCreateRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                EventValues.TIME_NOW);

        // MineCorePostCreateRequestEvent
        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCorePostCreateRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCorePostCreateRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                EventValues.TIME_NOW);
        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCorePostCreateRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCorePostCreateRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                EventValues.TIME_NOW);

        // MineCoreReceiveRequestEvent
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCoreReceiveRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCoreReceiveRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                EventValues.TIME_NOW);
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreReceiveRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreReceiveRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                EventValues.TIME_NOW);

        // MineCoreReceiveVoteEvent
        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                MappedVote.class,
                new Getter<MappedVote, MineCoreReceiveVoteEvent>() {
                    @Override
                    public MappedVote get(MineCoreReceiveVoteEvent e) {
                        return e.getVote();
                    }
                },
                EventValues.TIME_NOW);
        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreReceiveVoteEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreReceiveVoteEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                EventValues.TIME_NOW);
    }
}
