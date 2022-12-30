
public class Other {
	
	class Device{
		public void printStatus() {
			System.out.println("Device");
		}
	}
	
	class SoundDevice extends Device{
		public void printStatus() {
			System.out.println("SoundDevice");
		}
	}

	public static void main(String[] args) {
		new Other();
	}
	
	public Other() {
		Device d = new SoundDevice();
		d.printStatus();
	}

}
