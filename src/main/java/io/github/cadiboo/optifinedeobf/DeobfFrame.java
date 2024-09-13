package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;
import io.github.cadiboo.optifinedeobf.util.CustomFileFilter;
import io.github.cadiboo.optifinedeobf.util.FixedJarInputStream;
import io.github.cadiboo.optifinedeobf.util.Utils;
import org.objectweb.asm.ClassReader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class DeobfFrame extends JFrame {

	public static final FileFilter JAVA_FILE_FILTER = new CustomFileFilter("Jar and class files", f -> f.getName().endsWith(".class") || f.getName().endsWith(".jar"));
	public static final FileFilter MAPPINGS_FILE_FILTER = new CustomFileFilter("Mappings files", f -> f.getName().endsWith(".srg") || f.getName().endsWith(".tsrg") || f.getName().endsWith(".tsrg2"));

	private final JTextField inputFileTextField;
	private final JTextField outputFileTextField;
	private final JLabel progressTitle;
	private final JLabel progressSubTitle;
	private final JCheckBox makePublicCheckbox;
	private final JCheckBox definaliseCheckbox;
	private final JCheckBox makeForgeDevJarCheckbox;
	private final JTextField mappingsFileTextField;
	private MappingService mappingService = null;

	private DeobfFrame() throws HeadlessException {
		this.setName("DeobfFrame");
		this.setSize(400, 300);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setTitle("OptiFine Deobfuscator");

		var title = new JLabel();
		title.setName("title");
		title.setBounds(2, 5, 385, 42);
		title.setFont(new Font("Dialog", Font.BOLD, 18));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(385, 42));
		title.setText("OptiFine Deobfuscator");

		var description = new JLabel();
		description.setName("description");
		description.setBounds(2, 30, 385, 42);
		description.setFont(new Font("Dialog", Font.PLAIN, 12));
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setPreferredSize(new Dimension(385, 42));
		description.setText("This deobfuscator will deobfuscate (extracted) OptiFine");

		var inputDropTarget = createDropTarget(this::setInputFile);

		var inputFileLabel = new JLabel();
		inputFileLabel.setName("inputFileLabel");
		inputFileLabel.setBounds(15, 75, 60, 16);
		inputFileLabel.setPreferredSize(new Dimension(75, 16));
		inputFileLabel.setText("Input File");
		inputFileLabel.setDropTarget(inputDropTarget);

		inputFileTextField = new JTextField();
		inputFileTextField.setName("inputFileTextField");
		inputFileTextField.setBounds(90, 75, 210, 20);
		inputFileTextField.setEditable(false);
		inputFileTextField.setPreferredSize(new Dimension(210, 20));
		inputFileTextField.setDropTarget(inputDropTarget);

		var inputFileButton = new JButton();
		inputFileButton.setName("inputFileButton");
		inputFileButton.setBounds(300, 75, 75, 20);
		inputFileButton.setMargin(new Insets(2, 2, 2, 2));
		inputFileButton.setPreferredSize(new Dimension(75, 20));
		inputFileButton.setText("Choose...");
		inputFileButton.addActionListener(e -> chooseFile("Select jar to deobfuscate", this::setInputFile, new JFileChooser(), JAVA_FILE_FILTER));
		inputFileButton.setDropTarget(inputDropTarget);

		var outputDropTarget = createDropTarget(this::setOutputFile);

		var outputFileLabel = new JLabel();
		outputFileLabel.setName("outputFileLabel");
		outputFileLabel.setBounds(15, 100, 75, 16);
		outputFileLabel.setPreferredSize(new Dimension(75, 16));
		outputFileLabel.setText("Output File");
		outputFileLabel.setDropTarget(outputDropTarget);

		outputFileTextField = new JTextField();
		outputFileTextField.setName("outputFileTextField");
		outputFileTextField.setBounds(90, 100, 210, 20);
		outputFileTextField.setEditable(true);
		outputFileTextField.setPreferredSize(new Dimension(210, 20));
		outputFileTextField.setDropTarget(outputDropTarget);

		var outputFileButton = new JButton();
		outputFileButton.setName("outputFileButton");
		outputFileButton.setBounds(300, 100, 75, 20);
		outputFileButton.setMargin(new Insets(2, 2, 2, 2));
		outputFileButton.setPreferredSize(new Dimension(75, 20));
		outputFileButton.setText("Choose...");
		outputFileButton.addActionListener(e -> chooseFile("Select jar to deobfuscate", this::setOutputFile, new JFileChooser(), JAVA_FILE_FILTER));
		outputFileButton.setDropTarget(outputDropTarget);

		var mappingsDropTarget = createDropTarget(this::setMappingsFile);

		var mappingsFileLabel = new JLabel();
		mappingsFileLabel.setName("mappingsFileLabel");
		mappingsFileLabel.setBounds(15, 125, 75, 16);
		mappingsFileLabel.setPreferredSize(new Dimension(75, 16));
		mappingsFileLabel.setText("Mappings");
		mappingsFileLabel.setDropTarget(mappingsDropTarget);

		mappingsFileTextField = new JTextField("Choose Mappings...");
		mappingsFileTextField.setName("mappingsFileTextField");
		mappingsFileTextField.setBounds(90, 125, 210, 20);
		mappingsFileTextField.setEditable(false);
		mappingsFileTextField.setPreferredSize(new Dimension(210, 20));
		mappingsFileTextField.setDropTarget(mappingsDropTarget);

		var mappingsFileButton = new JButton();
		mappingsFileButton.setName("mappingsFileButton");
		mappingsFileButton.setBounds(300, 125, 75, 20);
		mappingsFileButton.setMargin(new Insets(2, 2, 2, 2));
		mappingsFileButton.setPreferredSize(new Dimension(75, 20));
		mappingsFileButton.setText("Choose...");
		mappingsFileButton.addActionListener(e -> this.chooseMappingsFile());
		mappingsFileButton.setDropTarget(mappingsDropTarget);

		progressTitle = new JLabel();
		progressTitle.setName("progressTitle");
		progressTitle.setBounds(15, 160, 360, 15);
		progressTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
		progressTitle.setHorizontalAlignment(SwingConstants.CENTER);
		progressTitle.setPreferredSize(new Dimension(360, 15));

		progressSubTitle = new JLabel();
		progressSubTitle.setName("progressSubTitle");
		progressSubTitle.setBounds(15, 175, 360, 15);
		progressSubTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
		progressSubTitle.setHorizontalAlignment(SwingConstants.CENTER);
		progressSubTitle.setPreferredSize(new Dimension(360, 15));

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

		makeForgeDevJarCheckbox = new JCheckBox();
		makeForgeDevJarCheckbox.setName("makeForgeDevJarCheckbox");
		makeForgeDevJarCheckbox.setBounds(0, 0, 200, 20);
		makeForgeDevJarCheckbox.setText("Forge Dev Jar");
		makeForgeDevJarCheckbox.setToolTipText("If OptiFine should have some tweaks applied to make it a valid for use in a Forge mod development workspace");

		var deobfButton = new JButton();
		deobfButton.setName("deobfButton");
		deobfButton.setBounds(350, 10, 0, 0);
		deobfButton.setMargin(new Insets(2, 2, 2, 2));
		deobfButton.setPreferredSize(new Dimension(50, 20));
		deobfButton.setText("Deobf");
		deobfButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				deobfButton.setEnabled(false);
				DeobfFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					deobf();
				} catch (Exception e) {
					handleException(e);
				} finally {
					DeobfFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					deobfButton.setEnabled(true);
				}
				return null;
			}
		}.execute());

		var centerPanel = new JPanel();
		centerPanel.setName("centerPanel");
		centerPanel.setLayout(null);

		var bottomPanel = new JPanel();
		bottomPanel.setName("bottomPanel");
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
		bottomPanel.setPreferredSize(new Dimension(400, 100));

		var contentPanel = new JPanel();
		contentPanel.setName("contentPanel");
		contentPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.setPreferredSize(new Dimension(400, 300));

		{
			centerPanel.add(title, title.getName());
			centerPanel.add(description, description.getName());
			centerPanel.add(inputFileLabel, inputFileLabel.getName());
			centerPanel.add(inputFileTextField, inputFileTextField.getName());
			centerPanel.add(inputFileButton, inputFileButton.getName());
			centerPanel.add(outputFileLabel, outputFileLabel.getName());
			centerPanel.add(outputFileTextField, outputFileTextField.getName());
			centerPanel.add(outputFileButton, outputFileButton.getName());
			centerPanel.add(mappingsFileLabel, mappingsFileLabel.getName());
			centerPanel.add(mappingsFileTextField, mappingsFileTextField.getName());
			centerPanel.add(mappingsFileButton, mappingsFileButton.getName());
			centerPanel.add(progressTitle, progressTitle.getName());
			centerPanel.add(progressSubTitle, progressSubTitle.getName());

			bottomPanel.add(makePublicCheckbox, makePublicCheckbox.getName());
			bottomPanel.add(definaliseCheckbox, definaliseCheckbox.getName());
			bottomPanel.add(makeForgeDevJarCheckbox, makeForgeDevJarCheckbox.getName());
			bottomPanel.add(deobfButton, deobfButton.getName());

			contentPanel.add(centerPanel, "Center");
			contentPanel.add(bottomPanel, "South");
		}

		this.setContentPane(contentPanel);
		this.pack();
	}

	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		var frame = new DeobfFrame();
		centreWindow(frame);
		frame.setVisible(true);
	}

	private static void centreWindow(DeobfFrame frame) {
		var rect = frame.getBounds();
		var scrDim = Toolkit.getDefaultToolkit().getScreenSize();
		var parRect = new Rectangle(0, 0, scrDim.width, scrDim.height);
		int newX = parRect.x + (parRect.width - rect.width) / 2;
		int newY = parRect.y + (parRect.height - rect.height) / 2;
		if (newX < 0)
			newX = 0;
		if (newY < 0)
			newY = 0;
		frame.setBounds(newX, newY, rect.width, rect.height);
	}

	private void handleException(Exception e) {
		if (e instanceof UserException userException) {
			setProgressText(userException.getMessage(), userException.subtitle);
		} else {
			e.printStackTrace();
			setProgressText("Error: " + e.getLocalizedMessage(), e.toString());
		}
	}

	private void setProgressText(String/*?*/ title, String/*?*/ subtitle) {
		progressTitle.setText(title);
		progressSubTitle.setText(subtitle);
	}

	private DropTarget createDropTarget(Consumer<File> handler) {
		return new DropTarget() {
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					for (var file : (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
						handler.accept(file);
					evt.dropComplete(true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		};
	}

	private void chooseFile(String title, Consumer<File> handler, JFileChooser chooser, FileFilter filter) {
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			handler.accept(chooser.getSelectedFile());
	}

	private void deobf() throws IOException {
		var input = Paths.get(inputFileTextField.getText()).toFile();
		var output = Paths.get(outputFileTextField.getText()).toFile();

		var classRemapper = new ClassRemapper(mappingService, makePublicCheckbox.isSelected(), definaliseCheckbox.isSelected());
		var fileRemapper = new FileRemapper(classRemapper, makeForgeDevJarCheckbox.isSelected());

		setProgressText("Remapping %s...".formatted(input.getName()), null);
		if (input.getName().endsWith(".class")) {
			try (
				var inputStream = new FileInputStream(input);
				var outputStream = new FileOutputStream(output)
			) {
				fileRemapper.deobfClass(inputStream, outputStream);
			}
		} else if (input.getName().endsWith(".jar")) {
			try (
				var inputStream = new FixedJarInputStream(input, false);
				var outputStream = new JarOutputStream(new FileOutputStream(output), inputStream.getManifest())
			) {
				fileRemapper.deobfJar(inputStream, outputStream, name -> progressSubTitle.setText("Processing %s...".formatted(name)));
			} catch (Exception e) {
				output.delete();
				throw e;
			}
		} else
			throw new IllegalStateException("Should not reach here - input file is invalid");
		setProgressText("Remapped file %s.".formatted(input.getName()), "Output: %s.".formatted(output.getName()));
	}

	private void chooseMappingsFile() {
		int result = JOptionPane.showConfirmDialog(null, "Would you like to select a project folder to search for mappings files?\nClick No to select a mappings file manually.", "Select project folder", JOptionPane.YES_NO_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			return;

		File startDirectory = null;
		if (result == JOptionPane.YES_OPTION) {
			var chooser = new JFileChooser();
			chooser.setDialogTitle("Select project folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			var mappingsFolder = Paths.get(chooser.getSelectedFile().getPath(), "build", "fg_cache", "de", "oceanlabs", "mcp", "mcp_config").toFile();
			if (mappingsFolder.isDirectory())
				startDirectory = mappingsFolder;
		}
		chooseFile("Select mappings file", this::setMappingsFile, new JFileChooser(startDirectory), MAPPINGS_FILE_FILTER);
	}

	private void setInputFile(File file) {
		setProgressText(null, null);
		var fileName = file.getName();
		if (fileName.endsWith(".jar")) {
			makeForgeDevJarCheckbox.setEnabled(true);
			if (!fileName.endsWith("MOD.jar"))
				setProgressText("Warning", "Input file does not look like an extracted OptiFine jar");
		} else if (fileName.endsWith(".class")) {
			makeForgeDevJarCheckbox.setSelected(false);
			makeForgeDevJarCheckbox.setEnabled(false);
		} else
			throw new UserException("Input file is not a jar or class file!", "Please select a valid input file.");
		var path = file.getPath();
		if (Objects.equals(outputFileTextField.getText(), defaultOutputFilePathForInput(inputFileTextField.getText())))
			setOutputFile(new File(defaultOutputFilePathForInput(path)));
		inputFileTextField.setText(path);
	}

	private String defaultOutputFilePathForInput(String path) {
		return path == null ? null : Utils.replaceLast(path, "\\.", "-deobf.");
	}

	private void setOutputFile(File file) {
		outputFileTextField.setText(file.getPath());
	}

	private void setMappingsFile(File file) {
		try {
			mappingService = createMappingsServiceFor(file);
			mappingsFileTextField.setText(file.getPath());
		} catch (Exception e) {
			handleException(e);
		}
	}

	private MappingService createMappingsServiceFor(File file) throws FileNotFoundException {
		var fileName = file.getName();
		if (fileName.endsWith(".srg"))
			return new SRG2MCP(new FileInputStream(file));
		if (fileName.endsWith(".tsrg") || fileName.endsWith(".tsrg2"))
			return new TSRG2MCP(new FileInputStream(file));
		throw new UserException("Mappings file is not a srg or tsrg file!", "Please select a valid mappings file.");
	}

	private static class UserException extends RuntimeException {
		public final String subtitle;

		public UserException(String title, String subtitle) {
			super(title);
			this.subtitle = subtitle;
		}
	}

	record FileRemapper(
		ClassRemapper remapper,
		boolean forgeDevJar
	) {

		private static final String OBFUSCATED_CLASS_PREFIX = "notch/";
		private static final String INTERMEDIARY_CLASS_PREFIX = "srg/";

		public void deobfClass(
			FileInputStream input,
			FileOutputStream output
		) throws IOException {
			output.write(remapper().remapClass(input.readAllBytes()));
			output.flush();
		}

		public void deobfJar(
			JarInputStream input,
			JarOutputStream output,
			Consumer<String> currentEntry
		) throws IOException {
			JarEntry jarEntry;
			while ((jarEntry = input.getNextJarEntry()) != null) {
				var jarEntryName = jarEntry.getName();
				currentEntry.accept(jarEntryName);

				if (!jarEntryName.endsWith(".class")) {
					output.putNextEntry(new JarEntry(jarEntry));
					output.write(Utils.readStreamFully(input));
					output.closeEntry();
					continue;
				}

				if (forgeDevJar() && jarEntryName.startsWith(OBFUSCATED_CLASS_PREFIX))
					continue;

				var remapped = remapper().remapClass(Utils.readStreamFully(input));
				// Intermediary files will have their names changed to deobfuscated names
				// E.g. 'srg/foo/class1234' -> 'foo/SomeClass'
				// Has the added benefit of getting rid of intermediary files automatically when making a dev jar
				var outputName = this.forgeDevJar() ? new ClassReader(remapped).getClassName() + ".class" : jarEntryName;

				output.putNextEntry(new JarEntry(outputName));
				output.write(remapped);
				output.closeEntry();
			}
			output.flush();
		}

	}

}
