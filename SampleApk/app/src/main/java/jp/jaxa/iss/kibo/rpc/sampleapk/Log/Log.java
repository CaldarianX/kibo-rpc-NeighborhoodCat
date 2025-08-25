package jp.jaxa.iss.kibo.rpc.sampleapk.Log;

public class Log {
    private boolean is_print;

    public Log(boolean is_print){
        this.is_print = is_print;
    }

    public void log(String message){
        try {
            if (is_print) {
                System.out.println(message);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
