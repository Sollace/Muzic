package com.sollace.muzic;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MuzicDiscRegistry {

    public static final MuzicDiscRegistry INSTANCE = new MuzicDiscRegistry();

    private final Map<SoundEvent, Optional<MuzicDisc>> soundLookup = new HashMap<>();
    private final Map<Integer, Optional<MuzicDisc>> hashLookup = new HashMap<>();

    final Loader clientResources = new Loader();
    final Loader serverData = new Loader();

    private MuzicDiscRegistry() {}

    public Optional<MuzicDisc> forId(Identifier id) {
        return Optional.ofNullable(
                Optional.ofNullable(serverData.entries.get(id))
                .orElseGet(() -> clientResources.entries.get(id)));
    }

    public Optional<MuzicDisc> forRawId(int id) {
        return hashLookup.computeIfAbsent(id, s -> Streams.stream(values())
                .filter(e -> s == e.getId().hashCode())
                .findAny());
    }

    public Optional<MuzicDisc> forSound(SoundEvent sound) {
        return soundLookup.computeIfAbsent(sound, s -> Streams.stream(values())
                .filter(e -> s.getId().equals(e.getSong()))
                .findAny());
    }

    public Iterable<MuzicDisc> values() {
        return Iterables.concat(serverData.entries.values(), clientResources.entries.values());
    }

    static final class Loader extends JsonDataLoader implements IdentifiableResourceReloadListener {
        private static final Identifier ID = new Identifier("muzic", "discs");
        private static final Gson GSON = new GsonBuilder()
                .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .registerTypeAdapter(Identifier.class, new TypeAdapter<Identifier>() {
                    @Override
                    public void write(JsonWriter out, Identifier value) throws IOException {
                        out.value(value.toString());
                    }

                    @Override
                    public Identifier read(JsonReader in) throws IOException {
                        return new Identifier(in.nextString());
                    }
                })
                .create();


        private Map<Identifier, MuzicDisc> entries = Collections.emptyMap();

        Loader() {
            super(GSON, "musicdisc");
        }

        @Override
        protected void apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler) {
            profiler.push("loading music discs");
            INSTANCE.soundLookup.clear();
            INSTANCE.hashLookup.clear();
            entries = data.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> {
                        Entry entry = GSON.fromJson(e.getValue(), Entry.class);
                        entry.id = e.getKey();
                        return entry;
                    }
            ));
            profiler.pop();
        }


        @Override
        public Identifier getFabricId() {
            return ID;
        }

        @Immutable
        static final class Entry implements MuzicDisc {

            @Expose
            private String description;

            @Expose
            private Identifier song;

            @Expose
            private int comparatorValue;

            @Expose
            private MuzicDiscPattern pattern;

            @Expose
            private int primaryColor;

            @Expose
            private int secondaryColor;

            private transient Identifier id;
            private transient final Lazy<SoundEvent> sound = new Lazy<>(() -> {
                if (!Registry.SOUND_EVENT.containsId(song)) {
                    Registry.register(Registry.SOUND_EVENT, song, new SoundEvent(song));
                }
                return Registry.SOUND_EVENT.get(song);
            });

            @Override
            public Identifier getId() {
                return id;
            }

            @Override
            public Text getDescription() {
                return new TranslatableText(description);
            }

            @Override
            public Identifier getSong() {
                return song;
            }

            @Override
            public SoundEvent getSound() {
                return sound.get();
            }

            @Override
            public int getComparatorOutput() {
                return comparatorValue;
            }

            @Override
            public MuzicDiscPattern getPattern() {
                if (pattern == null) {
                    return MuzicDiscPattern.NORMAL;
                }
                return pattern;
            }

            @Override
            public int getColor(int layer) {
                return layer == 0 ? 0xFFFFFF : primaryColor;
            }
        }
    }
}
