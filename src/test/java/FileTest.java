import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileTest {

	public static void main(String[] args) {
		for (int i = 0; i < 10000; i++) {
			save("./file.tst", "./temp.tst", i);
			int res = read("./file.tst");
			boolean result = i == res;
			if (result) {
				System.err.println(i+", "+res);
			}
		}
	}

	public static int read(String _file) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(_file));
			int i = (int) in.readObject();
			in.close();
			return i;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static void save(String _file, String _temp, int value) {
		try {
			File temp = new File(_temp);
			File file = new File(_file);
			if (!temp.getParentFile().exists()) {
				temp.getParentFile().mkdirs();
			}
			if (!temp.exists()) {
				temp.createNewFile();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(temp));
			out.writeObject(value);
			out.close();
			file.delete();
			temp.renameTo(file);
			temp.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
