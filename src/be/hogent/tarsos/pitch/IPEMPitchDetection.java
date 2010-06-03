package be.hogent.tarsos.pitch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import be.hogent.tarsos.util.AudioFile;
import be.hogent.tarsos.util.ConfKey;
import be.hogent.tarsos.util.Configuration;
import be.hogent.tarsos.util.Execute;
import be.hogent.tarsos.util.FileUtils;

/**
 * The IPEM Pitch detector uses an auditory model for polyphonic pitch tracking.
 * More information can be found in following papers: Factors affecting music
 * retrieval in query-by-melody De Mulder, Tom; Martens, Jean; PAUWS, S;
 * VIGNOLI, F et al. IEEE TRANSACTIONS ON MULTIMEDIA (2006) Recent improvements
 * of an auditory model based front-end for the transcription of vocal queries
 * De Mulder, Tom; MARTENS, J; Lesaffre, Micheline; Leman, Marc et al. 2004 IEEE
 * INTERNATIONAL CONFERENCE ON ACOUSTICS, SPEECH, AND SIGNAL PROCESSING, VOL IV,
 * PROCEEDINGS (2004) An Auditory Model Based Transcriber of Vocal Queries
 * 
 * De Mulder, Tom; Martens, Jean; Lesaffre, Micheline; Leman, Marc et al.
 * Proceedings of the Fourth International Conference on Music Information
 * Retrieval (ISMIR) 2003 (2003) The text file generated by the pitch detector
 * consists of 12 columns: 6 times a frequency in Hertz followed by a
 * probability. The frequencies are ordered by their respective probabilities.
 * 
 * @author Joren Six
 */
public final class IPEMPitchDetection implements PitchDetector {
    /**
     * Log messages.
     */
    private static final Logger LOG = Logger.getLogger(IPEMPitchDetection.class.getName());



    private final String name = "ipem";
    private final AudioFile file;

    private final List<Sample> samples;

    /**
     * @param file
     *            the file to detect pitch for
     */
    public IPEMPitchDetection(final AudioFile file) {
        this.file = file;
        this.samples = new ArrayList<Sample>();

        // check files and copy them if needed
        final String[] files = { "ipem_pitch_detection.sh", "libsndfile.dll", "pitchdetection.exe" };
        for (final String ipemFile : files) {
            final String target = FileUtils.combine(FileUtils.getRuntimePath(), ipemFile);
            if (!FileUtils.exists(target)) {
                FileUtils.copyFileFromJar("/be/hogent/tarsos/pitch/data/" + ipemFile, target);
            }
        }
    }

    @Override
    public void executePitchDetection() {

        final String transcodedBaseName = FileUtils.basename(file.transcodedPath());

        FileUtils.writeFile(transcodedBaseName + "\n", "lijst.txt");

        final String annotationsDirectory = Configuration.get(ConfKey.raw_ipem_annotations_directory);
        final String csvFileName = FileUtils.combine(FileUtils.getRuntimePath(), annotationsDirectory,
                transcodedBaseName + ".txt");
        String command = null;

        String audioDirectory = FileUtils.combine(AudioFile.TRANSCODED_AUDIO_DIR, "") + "/";
        String outputDirectory = FileUtils.combine(FileUtils.getRuntimePath(), annotationsDirectory) + "/";

        if (System.getProperty("os.name").contains("indows")) {
            audioDirectory = audioDirectory.replace("/", "\\").replace(":\\", "://");
            outputDirectory = outputDirectory.replace("/", "\\").replace(":\\", "://");
            command = "pitchdetection.exe  lijst.txt " + audioDirectory + " " + outputDirectory;

        } else { // on linux use wine's Z-directory
            audioDirectory = "z://" + audioDirectory.replace("/", "\\\\");
            outputDirectory = "z://" + outputDirectory.replace("/", "\\\\");
            audioDirectory = audioDirectory.replace("//\\\\", "//");
            outputDirectory = outputDirectory.replace("//\\\\", "//");
            command = FileUtils.getRuntimePath() + "/ipem_pitch_detection.sh \"" + audioDirectory + "\" \""
            + outputDirectory + "\"";
        }

        if (!FileUtils.exists(csvFileName)) {
            Execute.command(command, null);
        }

        final List<Double> probabilities = new ArrayList<Double>();
        final List<Double> pitches = new ArrayList<Double>();
        long start = 0;
        final double minimumAcceptableProbability = 0.05;

        final List<String[]> csvData = FileUtils.readCSVFile(csvFileName, " ", 12);

        for (final String[] row : csvData) {
            for (int index = 0; index < 6; index++) {
                Double probability = 0.0;
                try {
                    probability = Double.parseDouble(row[index * 2 + 1]);
                } catch (final NumberFormatException e) {
                    LOG.info("Ignored incorrectly formatted number: " + row[index * 2 + 1]);
                }

                Double pitch = row[index * 2].equals("-1.#IND00") || row[index * 2].equals("-1.#QNAN0") ? 0.0
                        : Double.parseDouble(row[index * 2]);
                // only accept values smaller than 25000Hz
                // bigger values are probably annotated incorrectly
                // With the ipem pitchdetector this happens sometimes, on wine
                // a big value is
                if (pitch > 25000) {
                    pitch = 0.0;
                }

                // Do not store 0 Hz values
                if (pitch != 0.0) {
                    probabilities.add(probability);
                    pitches.add(pitch);
                }
            }
            final Sample sample = new Sample(start, pitches, probabilities, minimumAcceptableProbability);
            sample.source = PitchDetectionMode.IPEM;
            samples.add(sample);
            start += 10;

            assert pitches.size() == 6;
            assert probabilities.size() == 6;

            probabilities.clear();
            pitches.clear();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Sample> getSamples() {
        return this.samples;
    }
}
