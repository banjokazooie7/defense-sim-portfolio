package io.github.banjokazooie7.simcore.output;

import io.github.banjokazooie7.simcore.event.EventListener;
import io.github.banjokazooie7.simcore.event.SimEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SimLogger implements EventListener {

    @FunctionalInterface
    public interface DetailSupplier {
        String detail(SimEvent event, long simTime);
    }

    private static final String HEADER = "sim_time_ms,event_type,priority,detail";

    private final List<String> rows;
    private DetailSupplier detailSupplier;

    public SimLogger() {
        this.rows = new ArrayList<>();
        this.detailSupplier = null;
    }

    public void setDetailSupplier(DetailSupplier supplier) {
        this.detailSupplier = supplier;
    }

    @Override
    public void onEvent(SimEvent event, long simTime) {
        String detail = "";
        if (detailSupplier != null) {
            detail = detailSupplier.detail(event, simTime);
            if (detail == null) detail = "";
        }
        detail = escapeCsv(detail);

        String row = String.format("%d,%s,%d,%s",
                simTime, event.getType(), event.getPriority(), detail);
        rows.add(row);
    }

    public int getRowCount() {
        return rows.size();
    }

    public String toCsvString() {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append('\n');
        for (String row : rows) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }

    public void flush(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(HEADER);
                writer.newLine();
                for (String row : rows) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write sim log to " + path, e);
        }
    }

    public void reset() {
        rows.clear();
    }

    static String escapeCsv(String value) {
        if (value.isEmpty()) return value;
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
