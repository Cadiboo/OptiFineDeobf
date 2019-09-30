package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class DeobfFrame extends JFrame {

	private final JTextField inputFileTextField;
	private final JTextField outputFileTextField;
	private final JCheckBox makePublicCheckbox;
	private final JCheckBox definaliseCheckbox;
	private final JCheckBox makeForgeDevJarCheckbox;
	private final JProgressBar progressBar;

	private MappingService mappingService = null;
	private ClassRemapper classRemapper = null;

	private DeobfFrame() throws HeadlessException {

		this.setName("DeobfFrame");
		this.setSize(404, 236);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setTitle("OptiFine Deobfuscator");

		JLabel title = new JLabel();
		title.setName("title");
		title.setBounds(2, 5, 385, 42);
		title.setFont(new Font("Dialog", Font.BOLD, 18));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(385, 42));
		title.setText("OptiFine Deobfuscator");

		JLabel description = new JLabel();
		description.setName("description");
		description.setBounds(2, 30, 385, 42);
		description.setFont(new Font("Dialog", Font.PLAIN, 12));
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setPreferredSize(new Dimension(385, 42));
		description.setText("This deobfuscator will deobfuscate (extracted) OptiFine");

		final JLabel inputFileLabel = new JLabel();
		inputFileLabel.setName("inputFileLabel");
		inputFileLabel.setBounds(15, 75, 60, 16);
		inputFileLabel.setPreferredSize(new Dimension(75, 16));
		inputFileLabel.setText("Input File");

		this.inputFileTextField = new JTextField();
		this.inputFileTextField.setName("inputFileTextField");
		this.inputFileTextField.setBounds(90, 75, 210, 20);
		this.inputFileTextField.setEditable(false);
		this.inputFileTextField.setPreferredSize(new Dimension(210, 20));

		final JButton inputFileButton = new JButton();
		inputFileButton.setName("inputFileButton");
		inputFileButton.setBounds(300, 75, 75, 20);
		inputFileButton.setMargin(new Insets(2, 2, 2, 2));
		inputFileButton.setPreferredSize(new Dimension(75, 20));
		inputFileButton.setText("Choose...");
		inputFileButton.addActionListener(e -> this.chooseInputFile());

		final JLabel outputFileLabel = new JLabel();
		outputFileLabel.setName("outputFileLabel");
		outputFileLabel.setBounds(15, 100, 75, 16);
		outputFileLabel.setPreferredSize(new Dimension(75, 16));
		outputFileLabel.setText("Output File");

		this.outputFileTextField = new JTextField();
		this.outputFileTextField.setName("outputFileTextField");
		this.outputFileTextField.setBounds(90, 100, 210, 20);
		this.outputFileTextField.setEditable(true);
		this.outputFileTextField.setPreferredSize(new Dimension(210, 20));

		final JButton outputFileButton = new JButton();
		outputFileButton.setName("outputFileButton");
		outputFileButton.setBounds(300, 100, 75, 20);
		outputFileButton.setMargin(new Insets(2, 2, 2, 2));
		outputFileButton.setPreferredSize(new Dimension(75, 20));
		outputFileButton.setText("Choose...");
		outputFileButton.addActionListener(e -> this.chooseOutputFile());

		this.progressBar = new JProgressBar();
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum(1);
		this.progressBar.setValue(1);
		this.progressBar.setBounds(15, 130, 360, 10);
		this.progressBar.setVisible(false);

		this.makePublicCheckbox = new JCheckBox();
		this.makePublicCheckbox.setName("makePublicCheckbox");
		this.makePublicCheckbox.setBounds(80, 100, 200, 20);
		this.makePublicCheckbox.setText("Public");
		this.makePublicCheckbox.setToolTipText("If classes, fields and methods should have their access escalated to public. Similar to an access transformer");

		this.definaliseCheckbox = new JCheckBox();
		this.definaliseCheckbox.setName("definaliseCheckbox");
		this.definaliseCheckbox.setBounds(80, 100, 200, 20);
		this.definaliseCheckbox.setText("Definalise");
		this.definaliseCheckbox.setToolTipText("If classes, fields and methods should have their access definalised. Similar to an access transformer");

		this.makeForgeDevJarCheckbox = new JCheckBox();
		this.makeForgeDevJarCheckbox.setName("makeForgeDevJarCheckbox");
		this.makeForgeDevJarCheckbox.setBounds(90, 100, 200, 20);
		this.makeForgeDevJarCheckbox.setText("Forge Dev");
		this.makeForgeDevJarCheckbox.setToolTipText("If OptiFine should have some tweaks applied to make it a valid for use in a Forge mod development workspace");

		final JButton deobfButton = new JButton();
		deobfButton.setName("deobfButton");
		deobfButton.setBounds(349, 10, 0, 0);
		deobfButton.setMargin(new Insets(2, 2, 2, 2));
		deobfButton.setPreferredSize(new Dimension(50, 20));
		deobfButton.setText("Deobf");
		deobfButton.addActionListener(e -> {
			// TODO: Why doesn't this work?
			this.progressBar.setVisible(true);
			deobfButton.setEnabled(false);
			this.deobf();
			this.progressBar.setVisible(false);
			deobfButton.setEnabled(true);
		});

		final JPanel centerPannel = new JPanel();
		centerPannel.setName("centerPannel");
		centerPannel.setLayout(null);

		final JPanel bottomPannel = new JPanel();
		bottomPannel.setName("bottomPannel");
		bottomPannel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
		bottomPannel.setPreferredSize(new Dimension(390, 55));

		final JPanel contentPanel = new JPanel();
		contentPanel.setName("contentPanel");
		contentPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.setPreferredSize(new Dimension(394, 203));

		{
			centerPannel.add(title, title.getName());
			centerPannel.add(description, description.getName());
			centerPannel.add(inputFileLabel, inputFileLabel.getName());
			centerPannel.add(this.inputFileTextField, this.inputFileTextField.getName());
			centerPannel.add(inputFileButton, inputFileButton.getName());
			centerPannel.add(outputFileLabel, outputFileLabel.getName());
			centerPannel.add(this.outputFileTextField, this.outputFileTextField.getName());
			centerPannel.add(outputFileButton, outputFileButton.getName());
			centerPannel.add(this.progressBar, this.progressBar.getName());

			bottomPannel.add(makePublicCheckbox, makePublicCheckbox.getName());
			bottomPannel.add(definaliseCheckbox, definaliseCheckbox.getName());
			bottomPannel.add(makeForgeDevJarCheckbox, makeForgeDevJarCheckbox.getName());
			bottomPannel.add(deobfButton, deobfButton.getName());

			contentPanel.add(centerPannel, "Center");
			contentPanel.add(bottomPannel, "South");
		}

		this.setContentPane(contentPanel);

		this.pack();

	}

	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		DeobfFrame frame = new DeobfFrame();
		// Centre Window
		{
			Rectangle rect = frame.getBounds();
			Rectangle parRect;
			Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
			parRect = new Rectangle(0, 0, scrDim.width, scrDim.height);
			int newX = parRect.x + (parRect.width - rect.width) / 2;
			int newY = parRect.y + (parRect.height - rect.height) / 2;
			if (newX < 0) {
				newX = 0;
			}
			if (newY < 0) {
				newY = 0;
			}
			frame.setBounds(newX, newY, rect.width, rect.height);
		}
		frame.setVisible(true);

	}

	private static byte[] readStreamFully(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, is.available()));
		byte[] buffer = new byte[8192];
		int read;
		while ((read = is.read(buffer)) >= 0) {
			baos.write(buffer, 0, read);
		}
		return baos.toByteArray();
	}

	private void deobf() {

		final boolean makeForgeDevJar = makeForgeDevJarCheckbox.isSelected();

		Path inputPath = Paths.get(this.inputFileTextField.getText());
		File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			return;
		}
		File outputFile = Paths.get(this.outputFileTextField.getText()).toFile();
		if (outputFile.exists()) {
			outputFile.delete();
		}

		if (mappingService == null)
			mappingService = new SRG2MCP();
		if (classRemapper == null || (classRemapper.mappingService != mappingService || classRemapper.makePublic != makePublicCheckbox.isSelected() || classRemapper.definalise != definaliseCheckbox.isSelected()))
			classRemapper = new ClassRemapper(mappingService, makePublicCheckbox.isSelected(), definaliseCheckbox.isSelected());

		try {
			if (inputFile.getName().endsWith(".jar")) {
				HashSet<String> filesToIngore = new HashSet<>();
				int filesToProcess = 0;
				try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(inputFile), false)) {
					JarEntry jarEntry;
					while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
						String jarEntryName = jarEntry.getName();
						++filesToProcess;
						if (makeForgeDevJar) {
							// Discard Forge dummy classes
							if (jarEntryName.startsWith("net/minecraftforge/"))
								filesToIngore.add(jarEntryName);
							// Discard Javax dummy class
							if (jarEntryName.startsWith("javax/"))
								filesToIngore.add(jarEntryName);
							// Discard notch named classes
							if (!jarEntryName.equals("Config.class") && jarEntryName.endsWith(".class") && !jarEntryName.contains("/"))
								filesToIngore.add(jarEntryName);
							// Mark notch-named classes with SRG counterparts as ignored so we don't try and add them to the zip twice
							if (jarEntryName.startsWith("srg/"))
								filesToIngore.add(jarEntryName.replaceFirst("srg/", ""));
						}
					}
				}

				this.progressBar.setMaximum(filesToProcess);

				try (
						JarInputStream jarInputStream = new JarInputStream(new FileInputStream(inputFile), false);
						JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile))
				) {
					JarEntry jarEntry;
					while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
						String jarEntryName = jarEntry.getName();

						this.progressBar.setValue(this.progressBar.getValue() + 1);
						this.progressBar.setToolTipText("Processing " + jarEntryName);

						if (filesToIngore.contains(jarEntryName))
							continue;
						if (jarEntryName.endsWith(".class")) {
							byte[] remappedBytes = classRemapper.remapClass(readStreamFully(jarInputStream));
							if (remappedBytes != null) {
								jarOutputStream.putNextEntry(new JarEntry(jarEntryName));
								jarOutputStream.write(remappedBytes);
								jarOutputStream.closeEntry();
								if (makeForgeDevJar && jarEntryName.startsWith("srg/")) {
									// Duplicate srg named classes and overwrite their notch named counterparts
									jarOutputStream.putNextEntry(new JarEntry(jarEntryName.replaceFirst("srg/", "")));
									jarOutputStream.write(remappedBytes);
									jarOutputStream.closeEntry();
								}
							}
						} else {
							jarOutputStream.putNextEntry(new JarEntry(jarEntry));
							jarOutputStream.write(readStreamFully(jarInputStream));
							jarOutputStream.closeEntry();
						}
					}
					jarOutputStream.flush();
				}

				this.progressBar.setMaximum(1);
				this.progressBar.setValue(1);
				this.progressBar.setToolTipText(null);
			} else if (inputFile.getName().endsWith(".class")) {
				this.progressBar.setMaximum(1);
				this.progressBar.setToolTipText("Processing " + inputFile.getName());

				byte[] remappedBytes = classRemapper.remapClass(Files.readAllBytes(inputPath));
				if (remappedBytes != null)
					Files.write(Paths.get(this.outputFileTextField.getText()), remappedBytes);

				this.progressBar.setValue(1);
				this.progressBar.setToolTipText(null);
			} else {
				throw new IllegalStateException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private JFileChooser makeInputChooser() {
		File startDir = new File("~/");
		JFileChooser chooser = new JFileChooser(startDir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(final File f) {
				String name = f.getName();
				return name.endsWith(".class") || name.endsWith(".jar");
			}

			@Override
			public String getDescription() {
				return "Jar and class files";
			}
		};
		chooser.setFileFilter(filter);
		return chooser;
	}

	private void chooseInputFile() {
		JFileChooser chooser = makeInputChooser();
		if (chooser.showOpenDialog(this) == 0) {
			File file = chooser.getSelectedFile();
			this.inputFileTextField.setText(file.getPath());
			this.outputFileTextField.setText(file.getParent() + File.separator + "deobf_" + file.getName());
		}
	}

	private void chooseOutputFile() {
		JFileChooser chooser = makeInputChooser();
		if (chooser.showSaveDialog(this) == 0) {
			File file = chooser.getSelectedFile();
			this.outputFileTextField.setText(file.getPath());
		}
	}

}
