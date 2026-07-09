package top.ialdaiaxiariyay.gtms.api.pattern;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads multiblock patterns from resource files, delegating to {@link MultiblockPatternBuilder}.
 * Supports only the new syntax: slice / sliceRepeatable.
 */
public class PatternBuilderLoader {

    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final MultiblockPatternBuilder builder;

    private PatternBuilderLoader(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection sliceDir) {
        // MultiblockPatternBuilder expects (sliceDir, stringDir, charDir)
        this.builder = MultiblockPatternBuilder.start(sliceDir, stringDir, charDir);
    }

    public PatternBuilderLoader slice(String... slice) {
        builder.slice(slice);
        return this;
    }

    public PatternBuilderLoader sliceRepeatable(int minRepeat, int maxRepeat, String... slice) {
        builder.sliceRepeatable(minRepeat, maxRepeat, slice);
        return this;
    }

    public PatternBuilderLoader where(char symbol, PatternPredicate predicate) {
        builder.where(symbol, predicate);
        return this;
    }

    public PatternBuilderLoader where(String symbol, PatternPredicate predicate) {
        return where(symbol.charAt(0), predicate);
    }

    public IBlockPattern build() {
        return builder.build();
    }

    @Contract(" -> new")
    public static @NotNull PatternBuilderLoader start() {
        return new PatternBuilderLoader(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull PatternBuilderLoader start(RelativeDirection charDir, RelativeDirection stringDir,
                                                      RelativeDirection sliceDir) {
        return new PatternBuilderLoader(charDir, stringDir, sliceDir);
    }

    /**
     * Loads a pattern from a resource file (uncached). The file must use the new syntax:
     * 
     * <pre>
     * direction(LEFT, UP, FRONT)   // optional, must appear before any slice lines
     * slice("AAA", "ABA", "AAA")
     * sliceRepeatable(3, "AAA", "AAA", "AAA")
     * sliceRepeatable(2, 4, "AAA", "AAA", "AAA")
     * </pre>
     * 
     * Lines starting with '#' are comments; empty lines are ignored.
     *
     * @param location resource location
     * @param charset  character set, typically StandardCharsets.UTF_8
     * @return a new PatternBuilderLoader instance with slices loaded but symbols unbound
     * @throws IOException if the resource cannot be read or parsing fails
     */
    public static @NotNull PatternBuilderLoader fromResource(@NotNull ResourceLocation location,
                                                             Charset charset) throws IOException {
        String resourcePath = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var is = PatternBuilderLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + location + " (path: " + resourcePath + ")");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                ParsedPattern parsed = parseRaw(reader, location.toString());
                return buildFromParsed(parsed);
            }
        }
    }

    /**
     * Loads (or retrieves from cache) a pattern from a resource file.
     * The file is parsed only once; subsequent calls return a new instance built from cached data.
     *
     * @param location resource location
     * @param charset  character set
     * @return a new PatternBuilderLoader instance (symbols unbound)
     * @throws UncheckedIOException if loading fails
     */
    public static @NotNull PatternBuilderLoader fromResourceCached(ResourceLocation location, Charset charset) {
        ParsedPattern parsed = PARSED_CACHE.computeIfAbsent(location, loc -> {
            try {
                return parseAndCache(loc, charset);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return buildFromParsed(parsed);
    }

    public static @NotNull PatternBuilderLoader fromResourceCached(ResourceLocation location) {
        return fromResourceCached(location, StandardCharsets.UTF_8);
    }

    private record ParsedPattern(List<String[]> depth,
                                 List<int[]> sliceRepetitions,
                                 RelativeDirection charDir,
                                 RelativeDirection stringDir,
                                 RelativeDirection sliceDir) {}

    private static final Map<ResourceLocation, ParsedPattern> PARSED_CACHE = new ConcurrentHashMap<>();

    @Contract("_, _ -> new")
    private static @NotNull ParsedPattern parseAndCache(ResourceLocation location, Charset charset) throws IOException {
        String resourcePath = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var is = PatternBuilderLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + location + " (path: " + resourcePath + ")");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                return parseRaw(reader, location.toString());
            }
        }
    }

    private static @NotNull PatternBuilderLoader buildFromParsed(ParsedPattern parsed) {
        PatternBuilderLoader loader = new PatternBuilderLoader(parsed.charDir, parsed.stringDir, parsed.sliceDir);
        List<String[]> depth = parsed.depth;
        List<int[]> reps = parsed.sliceRepetitions;
        for (int i = 0; i < depth.size(); i++) {
            String[] slice = depth.get(i);
            int[] rep = reps.get(i);
            if (rep[0] == 1 && rep[1] == 1) {
                loader.slice(slice);
            } else {
                loader.sliceRepeatable(rep[0], rep[1], slice);
            }
        }
        return loader;
    }

    /**
     * Parses the reader content. Only supports 'direction(...)' (optional) and 'slice' / 'sliceRepeatable' commands.
     */
    private static @NotNull ParsedPattern parseRaw(@NotNull BufferedReader reader,
                                                   String sourceName) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        RelativeDirection charDir = RelativeDirection.LEFT;
        RelativeDirection stringDir = RelativeDirection.UP;
        RelativeDirection sliceDir = RelativeDirection.FRONT;
        boolean directionFound = false;

        List<String> remainingLines = new ArrayList<>();
        for (String rawLine : lines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            if (trimmed.startsWith("direction(") && trimmed.endsWith(")")) {
                if (directionFound) {
                    throw new IOException(sourceName + ": duplicate direction directive");
                }
                String argsPart = trimmed.substring("direction(".length(), trimmed.length() - 1).trim();
                String[] parts = argsPart.split(",");
                if (parts.length != 3) {
                    throw new IOException(sourceName + ": direction requires exactly 3 arguments");
                }
                try {
                    charDir = RelativeDirection.valueOf(parts[0].trim().toUpperCase());
                    stringDir = RelativeDirection.valueOf(parts[1].trim().toUpperCase());
                    sliceDir = RelativeDirection.valueOf(parts[2].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IOException(sourceName + ": invalid direction name", e);
                }
                directionFound = true;
            } else {
                remainingLines.add(rawLine);
            }
        }

        List<String[]> depth = new ArrayList<>();
        List<int[]> sliceRepetitions = new ArrayList<>();

        int lineNum = 0;
        for (String rawLine : remainingLines) {
            lineNum++;
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            int parenIndex = trimmed.indexOf('(');
            if (parenIndex < 0) throw new IOException(sourceName + " line " + lineNum + ": missing '('");
            String funcName = trimmed.substring(0, parenIndex).trim();
            String argsPart = trimmed.substring(parenIndex + 1);
            if (!argsPart.endsWith(")")) throw new IOException(sourceName + " line " + lineNum + ": missing ')'");
            argsPart = argsPart.substring(0, argsPart.length() - 1).trim();

            List<String> args = parseArguments(argsPart, sourceName, lineNum);

            if (funcName.equals("slice")) {
                if (args.isEmpty())
                    throw new IOException(
                            sourceName + " line " + lineNum + ": slice needs at least one string argument");
                depth.add(args.toArray(new String[0]));
                sliceRepetitions.add(new int[] { 1, 1 });
            } else if (funcName.equals("sliceRepeatable")) {
                if (args.size() < 2)
                    throw new IOException(
                            sourceName + " line " + lineNum + ": sliceRepeatable needs at least 2 arguments");
                String firstArg = args.get(0);
                int min, max;
                if (firstArg.matches("\\d+")) {
                    int cnt = Integer.parseInt(firstArg);
                    min = max = cnt;
                    args = args.subList(1, args.size());
                } else if (firstArg.matches("\\d+,\\d+")) {
                    String[] parts = firstArg.split(",");
                    min = Integer.parseInt(parts[0]);
                    max = Integer.parseInt(parts[1]);
                    args = args.subList(1, args.size());
                } else {
                    throw new IOException(sourceName + " line " + lineNum + ": invalid repeat specifier: " + firstArg);
                }
                if (args.isEmpty())
                    throw new IOException(sourceName + " line " + lineNum +
                            ": sliceRepeatable needs at least one string argument after repeat specifier");
                depth.add(args.toArray(new String[0]));
                sliceRepetitions.add(new int[] { min, max });
            } else {
                throw new IOException(sourceName + " line " + lineNum + ": unknown function '" + funcName + "'");
            }
        }

        // Validate all slices have consistent dimensions
        if (!depth.isEmpty()) {
            int height = depth.get(0).length;
            int width = depth.get(0)[0].length();
            for (String[] slice : depth) {
                if (slice.length != height) {
                    throw new IOException(sourceName + ": inconsistent slice height (expected " + height + ", got " +
                            slice.length + ")");
                }
                for (String row : slice) {
                    if (row.length() != width) {
                        throw new IOException(sourceName + ": inconsistent row width (expected " + width + ", got " +
                                row.length() + ")");
                    }
                }
            }
        }

        return new ParsedPattern(depth, sliceRepetitions, charDir, stringDir, sliceDir);
    }

    /**
     * Parses a comma-separated argument list, supporting quoted strings with escapes.
     */
    private static @NotNull List<String> parseArguments(@NotNull String argsPart, String sourceName,
                                                        int lineNum) throws IOException {
        List<String> result = new ArrayList<>();
        int i = 0;
        int len = argsPart.length();
        while (i < len) {
            while (i < len && argsPart.charAt(i) == ' ') i++;
            if (i >= len) break;

            if (argsPart.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len && argsPart.charAt(i) != '"') {
                    if (argsPart.charAt(i) == '\\' && i + 1 < len) {
                        char next = argsPart.charAt(i + 1);
                        if (next == '"' || next == '\\') {
                            sb.append(next);
                            i += 2;
                            continue;
                        }
                    }
                    sb.append(argsPart.charAt(i));
                    i++;
                }
                if (i >= len) throw new IOException(sourceName + " line " + lineNum + ": unclosed string");
                i++;
                result.add(sb.toString());
            } else {
                int start = i;
                while (i < len && argsPart.charAt(i) != ',' && argsPart.charAt(i) != ' ') i++;
                String token = argsPart.substring(start, i).trim();
                result.add(token);
            }
            while (i < len && argsPart.charAt(i) == ' ') i++;
            if (i < len && argsPart.charAt(i) == ',') i++;
        }
        return result;
    }
}
