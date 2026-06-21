package dk.mineclub.minecore.hooks.skript.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/** Compatibility helper that lets a condition own and execute an indented Skript block. */
public abstract class EffectSection extends Condition {
    protected @Nullable SectionNode section = null;
    private @Nullable TriggerSection trigger = null;

    protected EffectSection() {
        captureSection();
    }

    private void captureSection() {
        Node node = SkriptLogger.getNode();
        if (!(node instanceof SectionNode)) {
            return;
        }

        String key = node.getKey() == null ? "" : node.getKey();
        String comment = readField(Node.class, node, "comment");
        if (comment == null) {
            comment = "";
        }

        SectionNode parent = node.getParent();
        section = new SectionNode(key, comment, parent == null ? null : parent, node.getLine());
        copyNodes(node, section);
    }

    @Override
    public boolean check(@Nullable Event event) {
        if (event == null) {
            return false;
        }

        execute(event);
        return !hasSection();
    }

    protected abstract void execute(@Nullable Event event);

    protected final void loadSection(boolean setNext) {
        if (section == null) {
            return;
        }

        RetainingLogHandler errors = SkriptLogger.startRetainingLog();
        try {
            trigger =
                    new TriggerSection(section) {
                        @Override
                        public String toString(Event event, boolean debug) {
                            return EffectSection.this.toString(event, debug);
                        }

                        @Override
                        protected TriggerItem walk(Event event) {
                            return walk(event, true);
                        }
                    };
            if (setNext) {
                trigger.setNext(getNext());
                setNext(null);
            }
        } finally {
            stopLog(errors);
            section = null;
        }
    }

    @SafeVarargs
    protected final void loadSection(
            @Nullable String name, boolean setNext, Class<? extends Event>... events) {
        if (section == null || events == null || events.length == 0) {
            return;
        }

        String previousName = ScriptLoader.getCurrentEventName();
        Class<? extends Event>[] previousEvents = ScriptLoader.getCurrentEvents();
        Kleenean previousDelay = ScriptLoader.hasDelayBefore;

        ScriptLoader.setCurrentEvent(name == null ? "minecore section" : name, events);
        try {
            loadSection(setNext);
        } finally {
            if (previousName == null || previousEvents == null) {
                ScriptLoader.deleteCurrentEvent();
            } else {
                ScriptLoader.setCurrentEvent(previousName, previousEvents);
            }
            ScriptLoader.hasDelayBefore = previousDelay;
        }
    }

    protected final boolean hasSection() {
        return section != null || trigger != null;
    }

    protected final void runSection(Event event) {
        if (trigger != null) {
            TriggerItem.walk(trigger, event);
        }
    }

    private static String readField(Class<?> owner, Object target, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(target);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Failed to read field '" + fieldName + "' from " + owner.getName(), ex);
        }
    }

    private static void copyNodes(Node source, SectionNode target) {
        try {
            Field field = SectionNode.class.getDeclaredField("nodes");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Node> nodes = (ArrayList<Node>) field.get(source);
            field.set(target, nodes);
            field.set(source, new ArrayList<Node>());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to copy SectionNode nodes", ex);
        }
    }

    private static void stopLog(RetainingLogHandler logger) {
        logger.stop();
        HandlerList handlers = readHandlers();
        if (handlers == null) {
            return;
        }

        Iterator<LogHandler> it = handlers.iterator();
        List<LogHandler> toStop = new ArrayList<>();
        while (it.hasNext()) {
            LogHandler handler = it.next();
            if (handler instanceof ParseLogHandler) {
                toStop.add(handler);
            } else {
                break;
            }
        }
        toStop.forEach(LogHandler::stop);
        SkriptLogger.logAll(logger.getLog());
    }

    private static @Nullable HandlerList readHandlers() {
        try {
            Field field = SkriptLogger.class.getDeclaredField("handlers");
            field.setAccessible(true);
            return (HandlerList) field.get(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to access Skript logger handlers", ex);
        }
    }
}
