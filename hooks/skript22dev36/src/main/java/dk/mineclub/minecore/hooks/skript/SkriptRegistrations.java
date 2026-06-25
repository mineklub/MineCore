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
import dk.mineclub.minecore.hooks.skript.effects.EffMineCoreCreateServerPay;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCorePostCreateRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCorePreCreateRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreReceiveRequest;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreReceiveVote;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreServerPayFailed;
import dk.mineclub.minecore.hooks.skript.events.EvtMineCoreServerPaySuccess;
import dk.mineclub.minecore.hooks.skript.events.MineCoreServerPayFailedEvent;
import dk.mineclub.minecore.hooks.skript.events.MineCoreServerPaySuccessEvent;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequest;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequestProducts;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreCreatedRequestProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreMappedVoteProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreNewProductProperty;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreNewRequestProducts;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreReceiveRequestType;
import dk.mineclub.minecore.hooks.skript.expressions.ExprMineCoreRequestProductProperty;
import dk.mineclub.minecore.hooks.skript.sections.SecMineCoreCreateRequest;
import dk.mineclub.minecore.platform.common.event.MineCorePostCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCorePreCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveVoteEvent;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

/** Registers MineCore syntax elements with Skript 2.2-dev36. */
final class SkriptRegistrations {
    private SkriptRegistrations() {}

    static void register() {
        registerEvents();
        registerEffects();
        registerExpressions();
        registerClasses();
        registerEventValues();
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
            Skript.registerEvent(
                    "MineCore Server Pay Failed",
                    EvtMineCoreServerPayFailed.class,
                    MineCoreServerPayFailedEvent.class,
                    "minecore server pay failed");
            Skript.registerEvent(
                    "MineCore Server Pay Success",
                    EvtMineCoreServerPaySuccess.class,
                    MineCoreServerPaySuccessEvent.class,
                    "minecore server pay success");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore events: " + e.getMessage());
        }
    }

    private static void registerEffects() {
        try {
            Skript.registerCondition(
                    SecMineCoreCreateRequest.class,
                    "create [a] minecore request for %offlineplayer%");
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
            Skript.registerEffect(
                    EffMineCoreCreateServerPay.class,
                    "create [a] [minecore] server pay for %offlineplayer% with amount %number%");
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
                                        return o.toString();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreRequest o) {
                                        return "newrequest:" + o;
                                    }

                                    @Override
                                    public String getVariableNamePattern() {
                                        return "newrequest:.+";
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
                                    public boolean canParse(@NonNull ParseContext context) {
                                        return false;
                                    }

                                    @Override
                                    public String toString(StoreCreatedRequest o, int flags) {
                                        return "request " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreCreatedRequest o) {
                                        return "createdrequest:" + o.getId();
                                    }

                                    @Override
                                    public String getVariableNamePattern() {
                                        return "createdrequest:.+";
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
                                        return "product " + o.getProductId();
                                    }

                                    @Override
                                    public String toVariableNameString(
                                            StoreCreatedRequest.Product o) {
                                        return "requestproduct:" + o.getProductId();
                                    }

                                    @Override
                                    public String getVariableNamePattern() {
                                        return "requestproduct:.+";
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
                                        return "new product " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(StoreProduct o) {
                                        return "newproduct:" + o.getId();
                                    }

                                    @Override
                                    public String getVariableNamePattern() {
                                        return "newproduct:.+";
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
                                        return "vote " + o.getId();
                                    }

                                    @Override
                                    public String toVariableNameString(MappedVote o) {
                                        return "mappedvote:" + o.getId();
                                    }

                                    @Override
                                    public String getVariableNamePattern() {
                                        return "mappedvote:.+";
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
                    "[the] id of minecore %requestproduct%",
                    "[the] name of minecore %requestproduct%",
                    "[the] price of minecore %requestproduct%",
                    "[the] quantity of minecore %requestproduct%",
                    "[the] created[ ]at of minecore %requestproduct%",
                    "[the] updated[ ]at of minecore %requestproduct%");
            Skript.registerExpression(
                    ExprMineCoreCreatedRequestProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of minecore %createdrequest%",
                    "[the] service of minecore %createdrequest%",
                    "[the] server status of minecore %createdrequest%",
                    "[the] client status of minecore %createdrequest%",
                    "[the] created[ ]at of minecore %createdrequest%",
                    "[the] updated[ ]at of minecore %createdrequest%");
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
                    "[the] id of minecore %newproduct%",
                    "[the] name of minecore %newproduct%",
                    "[the] price of minecore %newproduct%",
                    "[the] quantity of minecore %newproduct%",
                    "[the] subscription[ ]days of minecore %newproduct%");
            Skript.registerExpression(
                    ExprMineCoreMappedVoteProperty.class,
                    Object.class,
                    ExpressionType.PROPERTY,
                    "[the] id of minecore %mappedvote%",
                    "[the] status of minecore %mappedvote%",
                    "[the] created[ ]at of minecore %mappedvote%",
                    "[the] updated[ ]at of minecore %mappedvote%",
                    "[the] offlineplayer of minecore %mappedvote%");
        } catch (Exception e) {
            Skript.error("Failed to register MineCore expressions: " + e.getMessage());
        }
    }

    private static void registerEventValues() {
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                StoreRequest.class,
                new Getter<StoreRequest, MineCorePreCreateRequestEvent>() {
                    @Override
                    public StoreRequest get(MineCorePreCreateRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCorePreCreateRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCorePreCreateRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);

        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCorePostCreateRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCorePostCreateRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCorePostCreateRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCorePostCreateRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);

        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCoreReceiveRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCoreReceiveRequestEvent e) {
                        return e.getStoreRequest();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreReceiveRequestEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreReceiveRequestEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);

        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                MappedVote.class,
                new Getter<MappedVote, MineCoreReceiveVoteEvent>() {
                    @Override
                    public MappedVote get(MineCoreReceiveVoteEvent e) {
                        return e.getVote();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreReceiveVoteEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreReceiveVoteEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPayFailedEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreServerPayFailedEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreServerPayFailedEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPayFailedEvent.class,
                Number.class,
                new Getter<Number, MineCoreServerPayFailedEvent>() {
                    @Override
                    public Number get(MineCoreServerPayFailedEvent e) {
                        return e.getAmount();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPayFailedEvent.class,
                String.class,
                new Getter<String, MineCoreServerPayFailedEvent>() {
                    @Override
                    public String get(MineCoreServerPayFailedEvent e) {
                        return e.getMessage();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPaySuccessEvent.class,
                OfflinePlayer.class,
                new Getter<OfflinePlayer, MineCoreServerPaySuccessEvent>() {
                    @Override
                    public OfflinePlayer get(MineCoreServerPaySuccessEvent e) {
                        return e.getOfflinePlayer();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPaySuccessEvent.class,
                Number.class,
                new Getter<Number, MineCoreServerPaySuccessEvent>() {
                    @Override
                    public Number get(MineCoreServerPaySuccessEvent e) {
                        return e.getAmount();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreServerPaySuccessEvent.class,
                String.class,
                new Getter<String, MineCoreServerPaySuccessEvent>() {
                    @Override
                    public String get(MineCoreServerPaySuccessEvent e) {
                        return e.getMessage();
                    }
                },
                0);
    }
}
