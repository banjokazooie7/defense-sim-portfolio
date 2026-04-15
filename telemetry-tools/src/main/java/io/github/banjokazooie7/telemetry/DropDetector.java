package io.github.banjokazooie7.telemetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 
* Tracks telemetry packet sequence numbers and detects gaps
*
*When packets arrive out of order or are lost, the detector records
*whichs sequence numbers were missed. Post-Mission Analysis.
**/

public final class DropDetector{
    private int expectedNext;
    private long totalReceived;
    private long totalDropped;
    private final List<Integer> droppedSequences;
    private final List<Integer> seenSequences;

    public DropDetector(){
        this.expectedNext = 0;
        this.totalReceived = 0;
        this.totalDropped = 0;
        this.droppedSequences = new ArrayList<>();
        this.seenSequences = new ArrayList<>();
    }

    //Record a received packet. IF its sequence number is ahead of expected value
    //the gap is recorded as dropped packets
    public void record(TelemetryPacket packet){
        int seq = packet.sequenceNumber();

    //ignore duplicates
        if(seenSequences.contains(seq)){
            return;
        }
        seenSequences.add(seq);
        totalReceived++;

        //Late packet that was previously marked as dropped
        if(droppedSequences.contains(seq)){
            droppedSequences.remove(Integer.valueOf(seq));
            totalDropped--;
            return;
        }

        //Gap dtected: record missing sequence mumbers
        if(seq > expectedNext){
            for(int missed = expectedNext; missed < seq; missed++){
                if(!seenSequences.contains(missed)){
                    droppedSequences.add(missed);
                    totalDropped++;
                }
            }
        }

        if(seq >= expectedNext){
            expectedNext = seq + 1;
        }
    }

    public long getTotalReceived(){
        return totalReceived;
    }

    public long getTotalDropped(){
        return totalDropped;
    }
    public List<Integer> getDroppedSequences(){
        return Collections.unmodifiableList(droppedSequences);
    }

    public int getExpectedNext(){
        return expectedNext;
    }

    //Drop rate as a frcation: dropped / (dropped + received)
    //Returns 0.0 if nothing has been recorded yet
    public double getDropRate(){
        long total = totalReceived + totalDropped;
        if(total == 0) return 0.0;
        return (double) totalDropped / total;
    }

    public void reset(){
        expectedNext = 0;
        totalReceived = 0;
        totalDropped = 0;
        droppedSequences.clear();
    }

    public String toSummary(){
        return String.format(
            "DropDetector{received=%d, dropped=%d, dropRate=%.2f%%, gaps=%s}",
                totalReceived, totalDropped, getDropRate() * 100,
                droppedSequences.size() <= 10 ? droppedSequences.toString()
                        : droppedSequences.size() + " sequences");
    }
}