package main;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class MidiParser {

	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final String[] NOTE_NAMES = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};

	String[] instruments;
	String[][] sheets = new String[16][11];
	
	String filePath;
	
	


	public MidiParser(String fileName) {
		filePath = fileName;
	}

	public void parseFile(String filePath){
		
	}

	public void getNotes(String filePath, int instrumentIndex, int octaveTarget) throws Exception{

		for(int ins = 0; ins < 16; ins++){
			for(int oct = 0; oct < 11; oct++){
			sheets[ins][oct] = "";
			}
		}
		

		Sequence sequence = MidiSystem.getSequence(new File(filePath));
		TickConverter converter = new TickConverter(sequence);

		System.out.println("Sequence (MS): " + sequence.getMicrosecondLength()/1000);
		System.out.println("Sequence (ticks): " + sequence.getTickLength());
		System.out.println("Target octave: " + octaveTarget);	
		
		for(String i : instruments){
			System.out.println(i);
		}

		//if(true) return "";


		int trackNumber = 0;
		for (Track track :  sequence.getTracks()) {
			trackNumber++;
			//System.out.println("Track " + trackNumber + ": size = " + track.size());



			long prevTick = 0;
			boolean firstLineDone = false;

			for (int i=0; i < track.size(); i++) { 
				MidiEvent event = track.get(i);



				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					//System.out.println("Channel: " + sm.getChannel());

					if((event.getTick() - prevTick) < 3 && firstLineDone) continue;

					if (sm.getCommand() == NOTE_ON) {
						//int tick = Math.round((event.getTick() - prevTick)*sequence.getTickLength()/(sequence.getMicrosecondLength()/1000));
						String line = "";
						line += "w" + converter.ticksToMillis(event.getTick() - prevTick) + "\n";
						prevTick = event.getTick();

						int key = sm.getData1();
						int octave = (key / 12)-1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						//int velocity = sm.getData2();
						//System.out.println("Tick: " + event.getTick() + " Note on, " + noteName + octave + " key=" + key);
						//System.out.println(noteName);
						line += noteName;

//						if(octave < octaveTarget) sheets[sm.getChannel()] += "-1";
//						if(octave > octaveTarget) sheets[sm.getChannel()] += "+1";

						firstLineDone = true;

						
						for(int o = 0; o < 11; o++){
							sheets[sm.getChannel()][o] += line;
							if(octave < o) sheets[sm.getChannel()][o] += "-1";
							if(octave > o) sheets[sm.getChannel()][o] += "+1";
							sheets[sm.getChannel()][o] += "\n";
						}
						
					} 
				} 
			}
			//System.out.println();
		}

		return;

	}

	public void getInstruments(String filePath) throws Exception {
		
		Sequence sequence = MidiSystem.getSequence(new File(filePath));


		instruments = new String[16];

		for (Track track :  sequence.getTracks()) {

			for (int i=0; i < track.size(); i++) { 

				MidiEvent event = track.get(i);



				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {

						try {
							Synthesizer syn;

							syn = MidiSystem.getSynthesizer();
							//System.out.print("Channel: " + sm.getChannel() + " ");



							syn.open(); 
							Instrument[] instr = syn.getDefaultSoundbank().getInstruments();

							//System.out.println();

							Pattern pattern = Pattern.compile("Instrument: (.*?)  +bank.*");
							Matcher matcher = pattern.matcher(instr[sm.getData1()].toString());
							if(matcher.matches()){
								if(instruments[sm.getChannel()] == null) instruments[sm.getChannel()] = (sm.getChannel() + 1) + ". " + matcher.group(1);
								else instruments[sm.getChannel()] += ", " + matcher.group(1);

							}



							//System.out.print(((ShortMessage) message).getData1());
						} catch (MidiUnavailableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}


			}
		}
	}
	
	public String getSheet(int instrument, int octave){
		if(instrument == -1 || sheets[instrument][octave] == null) return "";
		else return sheets[instrument][octave];
	}
}
