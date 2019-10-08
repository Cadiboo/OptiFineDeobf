package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;
import io.github.cadiboo.optifinedeobf.util.CustomFileFilter;
import io.github.cadiboo.optifinedeobf.util.Utils;
import org.objectweb.asm.ClassReader;

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
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class DeobfFrame extends JFrame {

	public static final FileFilter JAVA_FILE_FILTER = new CustomFileFilter("Jar and class files", f -> f.getName().endsWith(".class") || f.getName().endsWith(".jar"));
	public static final FileFilter MAPPINGS_FILE_FILTER = new CustomFileFilter("Mappings files", f -> f.getName().endsWith(".srg") || f.getName().endsWith(".tsrg"));

	private static final int SRG_SLASH_LENGTH = "srg/".length();

	private final JTextField inputFileTextField;
	private final JTextField outputFileTextField;
	private final JProgressBar progressBar;
	private final JLabel progressLabel;
	private final JCheckBox makePublicCheckbox;
	private final JCheckBox definaliseCheckbox;
	private final JCheckBox remapFileNamesCheckbox;
	private final JCheckBox makeForgeDevJarCheckbox;
	private final JTextField mappingsFileTextField;
	private ClassRemapper classRemapper = null;
	private MappingService mappingService = null;

	private DeobfFrame() throws HeadlessException {

		this.setName("DeobfFrame");
		this.setSize(400, 300);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setTitle("OptiFine Deobfuscator");

		final JLabel title = new JLabel();
		title.setName("title");
		title.setBounds(2, 5, 385, 42);
		title.setFont(new Font("Dialog", Font.BOLD, 18));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(385, 42));
		title.setText("OptiFine Deobfuscator");

		final JLabel description = new JLabel();
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

		final DropTarget inputDropTarget = new DropTarget() {
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					for (File file : (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						setInputFile(file);
					}
					evt.dropComplete(true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		};

		inputFileTextField = new JTextField();
		inputFileTextField.setName("inputFileTextField");
		inputFileTextField.setBounds(90, 75, 210, 20);
		inputFileTextField.setEditable(false);
		inputFileTextField.setPreferredSize(new Dimension(210, 20));
		inputFileTextField.setDropTarget(inputDropTarget);

		final JButton inputFileButton = new JButton();
		inputFileButton.setName("inputFileButton");
		inputFileButton.setBounds(300, 75, 75, 20);
		inputFileButton.setMargin(new Insets(2, 2, 2, 2));
		inputFileButton.setPreferredSize(new Dimension(75, 20));
		inputFileButton.setText("Choose...");
		inputFileButton.addActionListener(e -> this.chooseInputFile());
		inputFileButton.setDropTarget(inputDropTarget);

		final JLabel outputFileLabel = new JLabel();
		outputFileLabel.setName("outputFileLabel");
		outputFileLabel.setBounds(15, 100, 75, 16);
		outputFileLabel.setPreferredSize(new Dimension(75, 16));
		outputFileLabel.setText("Output File");

		outputFileTextField = new JTextField();
		outputFileTextField.setName("outputFileTextField");
		outputFileTextField.setBounds(90, 100, 210, 20);
		outputFileTextField.setEditable(true);
		outputFileTextField.setPreferredSize(new Dimension(210, 20));

		final JButton outputFileButton = new JButton();
		outputFileButton.setName("outputFileButton");
		outputFileButton.setBounds(300, 100, 75, 20);
		outputFileButton.setMargin(new Insets(2, 2, 2, 2));
		outputFileButton.setPreferredSize(new Dimension(75, 20));
		outputFileButton.setText("Choose...");
		outputFileButton.addActionListener(e -> this.chooseOutputFile());

		final DropTarget mappingsDropTarget = new DropTarget() {
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					for (File file : (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						setMappings(file);
					}
					evt.dropComplete(true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		};

		final JLabel mappingsFileLabel = new JLabel();
		mappingsFileLabel.setName("mappingsFileLabel");
		mappingsFileLabel.setBounds(15, 125, 75, 16);
		mappingsFileLabel.setPreferredSize(new Dimension(75, 16));
		mappingsFileLabel.setText("Mappings");

		final String defaultMappings = new Random().nextBoolean() ? SRG2MCP.DEFAULT_MAPPINGS_FILE : TSRG2MCP.DEFAULT_MAPPINGS_FILE;

		mappingsFileTextField = new JTextField("Default (" + defaultMappings + ")");
		mappingsFileTextField.setName("mappingsFileTextField");
		mappingsFileTextField.setBounds(90, 125, 210, 20);
		mappingsFileTextField.setEditable(false);
		mappingsFileTextField.setPreferredSize(new Dimension(210, 20));
		mappingsFileTextField.setDropTarget(mappingsDropTarget);

		final JButton mappingsFileButton = new JButton();
		mappingsFileButton.setName("mappingsFileButton");
		mappingsFileButton.setBounds(300, 125, 75, 20);
		mappingsFileButton.setMargin(new Insets(2, 2, 2, 2));
		mappingsFileButton.setPreferredSize(new Dimension(75, 20));
		mappingsFileButton.setText("Choose...");
		mappingsFileButton.addActionListener(e -> this.chooseMappingsFile());
		mappingsFileButton.setDropTarget(mappingsDropTarget);

		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(1);
		progressBar.setValue(1);
		progressBar.setBounds(15, 150, 360, 10);
		progressBar.setVisible(false);

		progressLabel = new JLabel();
		progressLabel.setName("progressLabel");
		progressLabel.setBounds(15, 165, 360, 30);
		progressLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		progressLabel.setPreferredSize(new Dimension(360, 50));

		makePublicCheckbox = new JCheckBox();
		makePublicCheckbox.setName("makePublicCheckbox");
		makePublicCheckbox.setBounds(0, 0, 200, 20);
		makePublicCheckbox.setText("Make Public");
		makePublicCheckbox.setToolTipText("If classes, fields and methods should have their access escalated to public. Similar to an access transformer");

		definaliseCheckbox = new JCheckBox();
		definaliseCheckbox.setName("definaliseCheckbox");
		definaliseCheckbox.setBounds(0, 0, 200, 20);
		definaliseCheckbox.setText("Definalise");
		definaliseCheckbox.setToolTipText("If classes, fields and methods should have their access definalised. Similar to an access transformer");

		remapFileNamesCheckbox = new JCheckBox();
		remapFileNamesCheckbox.setName("remapFileNamesCheckbox");
		remapFileNamesCheckbox.setBounds(0, 0, 200, 20);
		remapFileNamesCheckbox.setText("Remap file names");
		remapFileNamesCheckbox.setToolTipText("If class files should be saved under their remapped name");

		makeForgeDevJarCheckbox = new JCheckBox();
		makeForgeDevJarCheckbox.setName("makeForgeDevJarCheckbox");
		makeForgeDevJarCheckbox.setBounds(0, 0, 200, 20);
		makeForgeDevJarCheckbox.setText("Forge Dev Jar");
		makeForgeDevJarCheckbox.setToolTipText("If OptiFine should have some tweaks applied to make it a valid for use in a Forge mod development workspace");
		makeForgeDevJarCheckbox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				this.remapFileNamesCheckbox.setSelected(false);
				this.remapFileNamesCheckbox.setEnabled(false);
			} else {
				this.remapFileNamesCheckbox.setEnabled(true);
			}
		});

		final JButton deobfButton = new JButton();
		deobfButton.setName("deobfButton");
		deobfButton.setBounds(350, 10, 0, 0);
		deobfButton.setMargin(new Insets(2, 2, 2, 2));
		deobfButton.setPreferredSize(new Dimension(50, 20));
		deobfButton.setText("Deobf");
		deobfButton.addActionListener(e -> {
			// TODO: Why isn't this visible?
			this.progressBar.setVisible(true);
			deobfButton.setEnabled(false);
			try {
				this.deobf();
				this.progressBar.setVisible(false);
			} finally {
				deobfButton.setEnabled(true);
			}
		});

		final JPanel centerPannel = new JPanel();
		centerPannel.setName("centerPannel");
		centerPannel.setLayout(null);

		final JPanel bottomPannel = new JPanel();
		bottomPannel.setName("bottomPannel");
		bottomPannel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
		bottomPannel.setPreferredSize(new Dimension(400, 100));

		final JPanel contentPanel = new JPanel();
		contentPanel.setName("contentPanel");
		contentPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.setPreferredSize(new Dimension(400, 300));

		{
			centerPannel.add(title, title.getName());
			centerPannel.add(description, description.getName());
			centerPannel.add(inputFileLabel, inputFileLabel.getName());
			centerPannel.add(inputFileTextField, inputFileTextField.getName());
			centerPannel.add(inputFileButton, inputFileButton.getName());
			centerPannel.add(outputFileLabel, outputFileLabel.getName());
			centerPannel.add(outputFileTextField, outputFileTextField.getName());
			centerPannel.add(outputFileButton, outputFileButton.getName());
			centerPannel.add(mappingsFileLabel, mappingsFileLabel.getName());
			centerPannel.add(mappingsFileTextField, mappingsFileTextField.getName());
			centerPannel.add(mappingsFileButton, mappingsFileButton.getName());
			centerPannel.add(progressBar, progressBar.getName());
			centerPannel.add(progressLabel, progressLabel.getName());

			bottomPannel.add(makePublicCheckbox, makePublicCheckbox.getName());
			bottomPannel.add(definaliseCheckbox, definaliseCheckbox.getName());
			bottomPannel.add(remapFileNamesCheckbox, remapFileNamesCheckbox.getName());
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

	private void handleException(final Exception e) {
		e.printStackTrace();
		progressLabel.setText("Error: " + e.getLocalizedMessage());
	}

	private void deobf() {

		Path inputPath = Paths.get(this.inputFileTextField.getText());
		File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			this.progressLabel.setText("Please select an input file.");
			return;
		}

		Path outputPath = Paths.get(this.outputFileTextField.getText());
		File outputFile = outputPath.toFile();
		if (outputFile.exists()) {
			if (!outputFile.delete()) {
				this.progressLabel.setText("Failed to delete pre-existing output file. Either delete it manually or choose a different output path.");
				return;
			}
		}

		if (mappingService == null) createMappingsService();
		if (classRemapper == null || (classRemapper.mappingService != mappingService || classRemapper.makePublic != makePublicCheckbox.isSelected() || classRemapper.definalise != definaliseCheckbox.isSelected()))
			classRemapper = new ClassRemapper(mappingService, makePublicCheckbox.isSelected(), definaliseCheckbox.isSelected());

		try {
			String inputFileName = inputFile.getName();
			if (inputFileName.endsWith(".jar"))
				remapJar(inputFile, outputFile);
			else if (inputFileName.endsWith(".class"))
				remapClass(inputPath, outputPath);
			else
				this.progressLabel.setText("Input file is not a jar or class file! Please select a valid input file.");
			this.progressLabel.setText("Successfully remapped file " + inputFileName);
		} catch (IOException e) {
			handleException(e);
		}

	}

	private void remapClass(final Path inputPath, final Path outputPath) throws IOException {
		byte[] remappedBytes = classRemapper.remapClass(Files.readAllBytes(inputPath));
		if (remappedBytes != null)
			Files.write(outputPath, remappedBytes);
	}

	private void remapJar(final File inputFile, final File outputFile) throws IOException {

		final boolean makeForgeDevJar = makeForgeDevJarCheckbox.isSelected();
		final boolean remapFileNames = remapFileNamesCheckbox.isSelected();

		HashSet<String> filesToIngore = new HashSet<>();
		final int filesToProcess = getFilesToProcess(inputFile, filesToIngore);

		this.progressBar.setMaximum(filesToProcess);

		try (
				JarInputStream jarInputStream = new JarInputStream(new FileInputStream(inputFile), false);
				JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile))
		) {
			if (makeForgeDevJar) {
				// Inject dummy OptiFine mod class to stop loading errors in dev
				jarOutputStream.putNextEntry(new JarEntry("optifine/OptiFineDeobfInjectedOptiFineModClass.class"));
				jarOutputStream.write(Utils.readStreamFully(getClass().getResourceAsStream("/OptiFineDeobfInjectedOptiFineModClass.class")));
				jarOutputStream.closeEntry();
			}
			JarEntry jarEntry;
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();

				this.progressBar.setValue(this.progressBar.getValue() + 1);
				this.progressBar.setToolTipText("Processing " + jarEntryName);

				if (filesToIngore.contains(jarEntryName))
					continue;

				if (jarEntryName.endsWith(".class")) {
					byte[] remappedBytes;
					try {
						remappedBytes = classRemapper.remapClass(Utils.readStreamFully(jarInputStream));
					} catch (Exception e) {
						handleException(e);
						throw e;
					}
					if (remappedBytes != null) {
						if (remapFileNames) jarOutputStream.putNextEntry(new JarEntry(new ClassReader(remappedBytes).getClassName() + ".class"));
						else jarOutputStream.putNextEntry(new JarEntry(jarEntryName));
						jarOutputStream.write(remappedBytes);
						jarOutputStream.closeEntry();

						if (makeForgeDevJar && jarEntryName.startsWith("srg/")) {
							// Duplicate srg named classes and overwrite their obf named counterparts
							jarOutputStream.putNextEntry(new JarEntry(jarEntryName.substring(SRG_SLASH_LENGTH)));
							jarOutputStream.write(remappedBytes);
							jarOutputStream.closeEntry();
						}
					}
				} else {
					jarOutputStream.putNextEntry(new JarEntry(jarEntry));
					jarOutputStream.write(Utils.readStreamFully(jarInputStream));
					jarOutputStream.closeEntry();
				}
			}
			jarOutputStream.flush();
		}

		this.progressBar.setMaximum(1);
		this.progressBar.setValue(1);
		this.progressBar.setToolTipText(null);
	}

	private int getFilesToProcess(final File inputFile, final HashSet<String> filesToIngore) throws IOException {
		final boolean makeForgeDevJar = makeForgeDevJarCheckbox.isSelected();

		int filesToProcess = 0;
		try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(inputFile), false)) {
			JarEntry jarEntry;
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				++filesToProcess;
				String jarEntryName = jarEntry.getName();
				if (makeForgeDevJar) {
					if (jarEntryName.startsWith("net/minecraftforge/") // Discard Forge dummy classes
							|| jarEntryName.startsWith("javax/") // Discard Javax dummy class
							|| (!jarEntryName.equals("Config.class") && jarEntryName.endsWith(".class") && !jarEntryName.contains("/")) // Discard obf named classes
					) {
						filesToIngore.add(jarEntryName);
					} else if (jarEntryName.startsWith("srg/")) { // Mark obf-named classes with SRG counterparts as ignored so we don't try and add them to the zip twice
						filesToIngore.add(jarEntryName.substring(SRG_SLASH_LENGTH));
					}
				}
			}
		}
		return filesToProcess;
	}

	private void createMappingsService() {
		String mappingsText = mappingsFileTextField.getText();
		File file = Paths.get(mappingsText).toFile();
		if (file.exists()) {
			setMappings(file);
		} else if (mappingsText.startsWith("Default")) {
			int dotIndex = mappingsText.lastIndexOf('.');
			if (dotIndex == -1)
				handleException(new IllegalStateException("Invalid default mappings input. What? How?"));
			String mappingsExt = mappingsText.substring(dotIndex + 1);
			if (mappingsExt.startsWith("srg"))
				mappingService = new SRG2MCP();
			else if (mappingsExt.startsWith("tsrg"))
				mappingService = new TSRG2MCP();
			else
				handleException(new IllegalStateException("Invalid default mappings input. What? How?"));
		} else {
			handleException(new IllegalStateException("Invalid mappings input"));
		}
	}

	private JFileChooser makeInputChooser(final FileFilter filter) {
		File startDir = new File("~/");
		JFileChooser chooser = new JFileChooser(startDir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(filter);
		return chooser;
	}

	private void chooseInputFile() {
		JFileChooser chooser = makeInputChooser(JAVA_FILE_FILTER);
		if (chooser.showOpenDialog(this) == 0) {
			setInputFile(chooser.getSelectedFile());
		}
	}

	private void chooseMappingsFile() {
		JFileChooser chooser = makeInputChooser(MAPPINGS_FILE_FILTER);
		if (chooser.showOpenDialog(this) == 0) {
			setMappings(chooser.getSelectedFile());
		}
	}

	private void chooseOutputFile() {
		JFileChooser chooser = makeInputChooser(JAVA_FILE_FILTER);
		if (chooser.showSaveDialog(this) == 0) {
			this.outputFileTextField.setText(chooser.getSelectedFile().getPath());
		}
	}

	private void setInputFile(final File file) {
		String fileName = file.getName();
		if (fileName.endsWith(".jar")) {
			this.outputFileTextField.setText(file.getParent() + File.separator + Utils.replaceLast(fileName, ".jar", "-deobf.jar"));

			this.makeForgeDevJarCheckbox.setEnabled(true);
			this.remapFileNamesCheckbox.setEnabled(true);
		} else if (fileName.endsWith(".class")) {
			this.outputFileTextField.setText(file.getParent() + File.separator + Utils.replaceLast(fileName, ".class", "-deobf.class"));

			this.makeForgeDevJarCheckbox.setSelected(false);
			this.makeForgeDevJarCheckbox.setEnabled(false);
			this.remapFileNamesCheckbox.setSelected(false);
			this.remapFileNamesCheckbox.setEnabled(false);
		} else {
			handleException(new IllegalStateException("Input file is not a jar or class file! Please select a valid input file."));
			return;
		}
		this.inputFileTextField.setText(file.getPath());
	}

	private void setMappings(final File file) {
		String fileName = file.getName();
		if (fileName.endsWith(".srg")) {
			try {
				this.mappingService = new SRG2MCP(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				handleException(e);
				return;
			}
		} else if (fileName.endsWith(".tsrg"))
			try {
				this.mappingService = new TSRG2MCP(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				handleException(e);
				return;
			}
		else {
			handleException(new IllegalStateException("Mappings file is not a srg or tsrg file! Please select a valid mappings file."));
			return;
		}
		this.mappingsFileTextField.setText(file.getPath());
	}

}
