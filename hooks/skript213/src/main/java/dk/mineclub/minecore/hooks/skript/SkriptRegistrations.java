package dk.mineclub.minecore.hooks.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
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
import dk.mineclub.minecore.hooks.skript.expressions.*;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import dk.mineclub.minecore.hooks.skript.sections.SecMineCoreCreateRequest;
import dk.mineclub.minecore.platform.common.event.MineCorePostCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCorePreCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveVoteEvent;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("UnstableApiUsage")
final class SkriptRegistrations {
    private SkriptRegistrations() {}

    static void register(SkriptAddon addon) {
        registerEvents(addon.syntaxRegistry());
        registerSections(addon.syntaxRegistry());
        registerEffects(addon.syntaxRegistry());
        registerExpressions(addon.syntaxRegistry());
        registerEventValues();
        registerClasses();
    }

    private static void registerEvents(SyntaxRegistry syntaxRegistry) {
        SyntaxRegistry.Key<BukkitSyntaxInfos.Event<?>> eventKey = SyntaxRegistry.Key.of("event");

        syntaxRegistry.register(
                eventKey,
                BukkitSyntaxInfos.Event.builder(
                                EvtMineCorePreCreateRequest.class, "MineCore Pre Create Request")
                        .addPatterns("minecore pre create request")
                        .addEvent(MineCorePreCreateRequestEvent.class)
                        .build());

        syntaxRegistry.register(
                eventKey,
                BukkitSyntaxInfos.Event.builder(
                                EvtMineCorePostCreateRequest.class, "MineCore Post Create Request")
                        .addPatterns("minecore post create request")
                        .addEvent(MineCorePostCreateRequestEvent.class)
                        .build());

        syntaxRegistry.register(
                eventKey,
                BukkitSyntaxInfos.Event.builder(
                                EvtMineCoreReceiveRequest.class, "MineCore Receive Request")
                        .addPatterns("minecore receive request")
                        .addEvent(MineCoreReceiveRequestEvent.class)
                        .build());

        syntaxRegistry.register(
                eventKey,
                BukkitSyntaxInfos.Event.builder(
                                EvtMineCoreReceiveVote.class, "MineCore Receive Vote")
                        .addPatterns("minecore receive vote")
                        .addEvent(MineCoreReceiveVoteEvent.class)
                        .build());
    }

    private static void registerSections(SyntaxRegistry syntaxRegistry) {
        syntaxRegistry.register(
                SyntaxRegistry.SECTION,
                SyntaxInfo.builder(SecMineCoreCreateRequest.class)
                        .addPattern("create [a] minecore request for %offlineplayer%")
                        .build());
    }

    private static void registerEffects(SyntaxRegistry syntaxRegistry) {
        syntaxRegistry.register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffMineCoreAddProduct.class)
                        .addPatterns(
                                "add [a] product %string% [named %-string%] [price %-number%] [quantity %-number%] [subscription[ ]days %-number%] [to [the] [minecore] request]")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffMineCoreAcceptRequest.class)
                        .addPatterns(
                                "accept [minecore] request %object%",
                                "accept [minecore] request [with] id %string%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffMineCoreAcceptVote.class)
                        .addPatterns(
                                "accept [minecore] vote %object%",
                                "accept [minecore] vote [with] id %string%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffMineCoreCancelRequest.class)
                        .addPatterns(
                                "cancel [minecore] request %object%",
                                "cancel [minecore] request [with] id %string%")
                        .build());
    }

    private static void registerExpressions(SyntaxRegistry syntaxRegistry) {
        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(ExprMineCoreReceiveRequestType.class, String.class)
                        .priority(SyntaxInfo.SIMPLE)
                        .addPatterns("[the] event type", "[the] event-type")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(
                                ExprMineCoreCreatedRequest.class, StoreCreatedRequest.class)
                        .addPatterns(
                                "[the] [last] [minecore] created request",
                                "[the] minecore request from [the] create section")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(
                                ExprMineCoreRequestProductProperty.class, Object.class)
                        .addPatterns(
                                "[the] id of %requestproduct%",
                                "[the] name of %requestproduct%",
                                "[the] price of %requestproduct%",
                                "[the] quantity of %requestproduct%",
                                "[the] created[ ]at of %requestproduct%",
                                "[the] updated[ ]at of %requestproduct%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(
                                ExprMineCoreCreatedRequestProperty.class, Object.class)
                        .addPatterns(
                                "[the] id of %createdrequest%",
                                "[the] service of %createdrequest%",
                                "[the] server status of %createdrequest%",
                                "[the] client status of %createdrequest%",
                                "[the] created[ ]at of %createdrequest%",
                                "[the] updated[ ]at of %createdrequest%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(
                                ExprMineCoreNewRequestProducts.class, StoreProduct.class)
                        .addPatterns("[the] [new] products of %newrequest%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(
                                ExprMineCoreCreatedRequestProducts.class,
                                StoreCreatedRequest.Product.class)
                        .addPatterns("[the] [new] products of %createdrequest%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(ExprMineCoreNewProductProperty.class, Object.class)
                        .addPatterns(
                                "[the] id of %newproduct%",
                                "[the] name of %newproduct%",
                                "[the] price of %newproduct%",
                                "[the] quantity of %newproduct%",
                                "[the] subscription[ ]days of %newproduct%")
                        .build());

        syntaxRegistry.register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(ExprMineCoreMappedVoteProperty.class, Object.class)
                        .addPatterns(
                                "[the] id of %mappedvote%",
                                "[the] status of %mappedvote%",
                                "[the] created[ ]at of %mappedvote%",
                                "[the] updated[ ]at of %mappedvote%",
                                "[the] offlineplayer of %mappedvote%")
                        .build());
    }

    private static void registerEventValues() {
        registerPreCreateRequestValues();
        registerPostCreateRequestValues();
        registerReceiveRequestValues();
        registerReceiveVoteValues();
    }

    private static void registerPreCreateRequestValues() {
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                OfflinePlayer.class,
                MineCorePreCreateRequestEvent::getOfflinePlayer);

        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                StoreRequest.class,
                MineCorePreCreateRequestEvent::getStoreRequest);
    }

    private static void registerPostCreateRequestValues() {
        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                OfflinePlayer.class,
                MineCorePostCreateRequestEvent::getOfflinePlayer);

        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                StoreCreatedRequest.class,
                MineCorePostCreateRequestEvent::getStoreRequest);
    }

    private static void registerReceiveRequestValues() {
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                OfflinePlayer.class,
                MineCoreReceiveRequestEvent::getOfflinePlayer);

        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                StoreCreatedRequest.class,
                MineCoreReceiveRequestEvent::getStoreRequest);
    }

    private static void registerReceiveVoteValues() {
        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                OfflinePlayer.class,
                MineCoreReceiveVoteEvent::getOfflinePlayer);

        EventValues.registerEventValue(
                MineCoreReceiveVoteEvent.class,
                MappedVote.class,
                MineCoreReceiveVoteEvent::getVote);
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
                                new Parser<>() {
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
                                new Parser<>() {
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
                                new Parser<>() {
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
                                new Parser<>() {
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
                                new Parser<>() {
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
}
