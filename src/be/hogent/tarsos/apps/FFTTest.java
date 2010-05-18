package be.hogent.tarsos.apps;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import be.hogent.tarsos.util.AudioFile;
import be.hogent.tarsos.util.FileUtils;
import be.hogent.tarsos.util.SimplePlot;

import com.sun.media.sound.AudioFloatInputStream;

public final class FFTTest {

    public static void main(String... args) throws UnsupportedAudioFileException, IOException {
        AudioFile audioFile = new AudioFile(FileUtils.combine("src", "be", "hogent", "tarsos", "test",
                "data", "power_test.wav"));
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(audioFile.path()));
        AudioFloatInputStream afis = AudioFloatInputStream.getInputStream(stream);

        int readAmount = 16384 / 2;
        float[] buffer = new float[readAmount];
        double[] bufferD = new double[readAmount];
        SimplePlot plot = new SimplePlot();
        AudioFormat format = stream.getFormat();

        double sampleRate = format.getSampleRate();

        int index = 0;

        double[] spectrum = new double[readAmount];

        while (afis.read(buffer, 0, readAmount) != -1) {

            for (int i = 0; i < buffer.length; i++) {
                bufferD[i] = buffer[i];
            }

            int numberOfFilledBins = 0;
            Complex[] data = new FastFourierTransformer().transform(bufferD);
            double maxAmplitude = -1;
            double indexOfMostEnergyRichFrequencyBin = -1;
            for (int j = 0; j < data.length / 2; j++) {
                double amplitude = data[j].getReal() * data[j].getReal() + data[j].getImaginary()
                * data[j].getImaginary();
                amplitude = Math.pow(amplitude, 0.5) / data.length;
                if (amplitude > maxAmplitude) {
                    maxAmplitude = amplitude;
                    indexOfMostEnergyRichFrequencyBin = j;
                }
                spectrum[j] = +amplitude;
                if (amplitude > 0.001) {
                    numberOfFilledBins++;
                }
            }
            if (numberOfFilledBins > data.length / 3) {
                System.out.println(index + " Is percussive");
            }

            double mostEnergyRichPitch = indexOfMostEnergyRichFrequencyBin * sampleRate / readAmount; // in
            // Hz
            plot.addData(index, mostEnergyRichPitch);
            index++;
        }
        plot.save();

        SimplePlot spectrumPlot = new SimplePlot();
        for (int i = 0; i < buffer.length / 2; i++) {
            spectrumPlot.addData(i * sampleRate / readAmount, spectrum[i]);
        }
        spectrumPlot.setXRange(0, 880);
        spectrumPlot.save();
    }
}
