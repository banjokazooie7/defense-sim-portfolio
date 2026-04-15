package io.github.banjokazooie7.telemetry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TelemetryLogger{
    private static final String HEADER = 
        "seq,sim_time_ms,entity_id,x_meters,y_meters,vx_m_s,vy_m_s,event_type";
        
    private final List<String> rows;

    public TelemetryLogger(){
        this.rows = Collections.synchronizedList(new ArrayList<>());
    }

    // Record one packet as a CSV row
    public void log(TelemetryPacket packet) {
        StringBuilder sb = new StringBuilder(128); // Pre-size for performance
        sb.append(packet.sequenceNumber()).append(',')
            .append(packet.timestampMillis()).append(',')
            .append(escapeCsv(packet.entityId())).append(',')
            .append(packet.xMeters()).append(',') // Appending doubles directly is faster
            .append(packet.yMeters()).append(',')
            .append(packet.vxMetersPerSec()).append(',')
            .append(packet.vyMetersPerSec()).append(',')
            .append(escapeCsv(packet.eventType()));
    
        // If you switch to streaming, you'd write sb.toString() to the BufferedWriter here
        rows.add(sb.toString()); 
    }

    public void logAll(List<TelemetryPacket> packets){
        for(TelemetryPacket pkt : packets){
            log(pkt);
        }
    }

    public int getRowCount(){
        return rows.size();
    }

    public List<String> getRows(){
        return Collections.unmodifiableList(rows);
    }

    public String toCsvString(){
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append('\n');
        for(String row : rows){
            sb.append(row).append('\n');
        }
        return sb.toString();
    }

    public void flush(Path path) {
        try {
            Path parent = path.normalize().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(HEADER);
                writer.newLine();
                for (String row : rows) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write telemetry log to " + path, e);
        }
    }

    public void reset(){
        rows.clear();
    }

    static String escapeCsv(String value){
        if(value.isEmpty()) return value;
        if(value.contains(",") || value.contains("\"") || value.contains("\n")){
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}