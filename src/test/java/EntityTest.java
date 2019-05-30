

import com.freydema.foodbot.commands.Start;
import org.junit.Test;

public class EntityTest {

    @Test
    public void test(){
        Start cmd = new Start(1L);
        cmd.getChatId();
        System.out.println(cmd);


    }
}
