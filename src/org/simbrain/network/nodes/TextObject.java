package org.simbrain.network.nodes;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.actions.CopyAction;
import org.simbrain.network.actions.CutAction;
import org.simbrain.network.actions.DeleteAction;
import org.simbrain.network.actions.GroupAction;
import org.simbrain.network.actions.PasteAction;
import org.simbrain.network.actions.SetNeuronPropertiesAction;
import org.simbrain.network.actions.SetSourceNeuronsAction;
import org.simbrain.network.actions.UngroupAction;
import org.simbrain.network.actions.connection.ConnectNeuronsAction;
import org.simbrain.network.actions.connection.ConnectNeuronsSimpleAction;
import org.simbrain.network.actions.connection.ShowConnectDialogAction;
import org.simbrain.workspace.Workspace;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * An editable text object.
 */
public class TextObject extends ScreenElement implements PropertyChangeListener {

    /** The text object. */
    private PStyledText ptext;

    /**
     * Construct text object at specified location.
     *
     * @param netPanel reference to networkPanel
     * @param ptext the styled text
     */
    public TextObject(final NetworkPanel netPanel, final PStyledText ptext) {
        super(netPanel);
        this.ptext = ptext;
        //ptext.setPickable(false); // otherwise the child rather than the parent is picked
        this.addChild(ptext);
        this.setBounds(ptext.getBounds());
        addPropertyChangeListener(PROPERTY_FULL_BOUNDS, this);
    }

    /** @Override. */
    public boolean isSelectable() {
        // TODO Auto-generated method stub
        return true;
    }

    /** @Override. */
    public boolean showSelectionHandle() {
        // TODO Auto-generated method stub
        return true;
    }

    /** @Override. */
    public boolean isDraggable() {
        // TODO Auto-generated method stub
        return true;
    }

    /** @Override. */
    protected boolean hasToolTipText() {
        // TODO Auto-generated method stub
        return false;
    }

    /** @Override. */
    protected String getToolTipText() {
        // TODO Auto-generated method stub
        return null;
    }

    /** @Override. */
    protected boolean hasContextMenu() {
        // TODO Auto-generated method stub
        return true;
    }

    /** @Override. */
    protected JPopupMenu getContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        contextMenu.add(new CutAction(getNetworkPanel()));
        contextMenu.add(new CopyAction(getNetworkPanel()));
        contextMenu.add(new PasteAction(getNetworkPanel()));
        contextMenu.addSeparator();

        contextMenu.add(new GroupAction(getNetworkPanel()));
        contextMenu.addSeparator();

        contextMenu.add(new DeleteAction(getNetworkPanel()));
        contextMenu.addSeparator();

       contextMenu.add(new SetNeuronPropertiesAction(getNetworkPanel()));

       return contextMenu;

  }

    /** @Override. */
    protected boolean hasPropertyDialog() {
        return false;
    }

    /** @Override. */
    protected JDialog getPropertyDialog() {
        // TODO Auto-generated method stub
        return null;
    }

    /** @Override. */
    public void resetColors() {
        // TODO Auto-generated method stub
        
    }

    public void propertyChange(PropertyChangeEvent arg0) {
	setBounds(ptext.getBounds());
    }

    /**
     * @return the ptext
     */
    public PStyledText getPtext() {
        return ptext;
    }

    /**
     * @param ptext the ptext to set
     */
    public void setPtext(PStyledText ptext) {
        this.ptext = ptext;
    }
}