package fastlayer.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.backtype.support.Utils;
import masterdataset.DataStore;
import org.apache.hadoop.fs.FileSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TweetSpout extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private List<String> records;
    private int dbcounter;
    private FileSystem fs;

    //open is called during initialization by storm and the SpoutOutputCollector is where the output will be sent
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.fs = DataStore.configureHDFS();

        this.collector = collector;
        this.dbcounter = 0;
        String filename = "dbFast15.txt";
        this.records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
        }
    }

    //colled again during initialization to know how the tuples generated are structured
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("line"));
    }

    public void nextTuple() {
        Utils.sleep(100);
        String line = records.get(dbcounter);
        if (dbcounter < records.size() - 1) {
            dbcounter++;
            try {
                DataStore.createAppendHDFS(fs, "tweet/newData/newTweet.txt", line);
            } catch (IOException e) {
                System.out.println("Error while appending newTweet");
            }
            collector.emit(new Values(line));
        }
    }
}
