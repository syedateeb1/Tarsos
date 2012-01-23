/**
*
*  Tarsos is developed by Joren Six at 
*  The Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*
**/
package be.hogent.tarsos.cli;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.hogent.tarsos.Tarsos;
import be.hogent.tarsos.sampled.pitch.Annotation;
import be.hogent.tarsos.sampled.pitch.PitchDetectionMode;
import be.hogent.tarsos.sampled.pitch.PitchDetector;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;
import be.hogent.tarsos.util.AudioFile;

/**
 * Detects pitch for an input file using a pitch detector. Outputs two columns,
 * one in ms and the oters in Hz.
 * 
 * @author Joren Six
 */
public final class DetectPitch extends AbstractTarsosApp {

	@Override
	public String description() {
		return "Detects pitch for an input file"
				+ " using a pitch detector. Outputs two columns, one in ms and the oters in Hz.";
	}

	@Override
	public String name() {
		return "detect_pitch";
	}

	@Override
	public void run(final String... args) {
		final OptionParser parser = new OptionParser();
		final OptionSpec<File> fileSpec = parser.accepts("in", "The file to annotate").withRequiredArg()
				.ofType(File.class).withValuesSeparatedBy(' ').defaultsTo(new File("in.wav"));
		final OptionSpec<PitchDetectionMode> detectionModeSpec = createDetectionModeSpec(parser);

		final OptionSet options = parse(args, parser, this);

		if (isHelpOptionSet(options)) {
			printHelp(parser);
		} else {
			final File inputFile = options.valueOf(fileSpec);
			final PitchDetectionMode detectionMode = options.valueOf(detectionModeSpec);
			AudioFile audioFile;
			try {
				audioFile = new AudioFile(inputFile.getAbsolutePath());
				final PitchDetector detector = detectionMode.getPitchDetector(audioFile);
				detector.executePitchDetection();
				for (final Annotation sample : detector.getAnnotations()) {
					Tarsos.println(sample.toString());
				}
			} catch (EncoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
