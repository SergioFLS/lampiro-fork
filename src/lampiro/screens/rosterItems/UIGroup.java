package lampiro.screens.rosterItems;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import lampiro.screens.RosterScreen;

import it.yup.ui.UIAccordion;
import it.yup.ui.UICanvas;
import it.yup.ui.UIConfig;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xml.BProcessor;
import it.yup.xml.Element;
import it.yup.client.Config;
import it.yup.xmpp.MUC;
import it.yup.xmpp.Roster;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class UIGroup extends UILabel {

	public static final int BEGINNING = 0;
	public static final int MIDDLE = 1;
	public static final int END = 2;

	/*
	 * Shows if the contact must be put at th start in the middle or at the end of the roster
	 */
	int groupConstraint = MIDDLE;

	static ResourceManager rm = ResourceManager.getManager();

	public static String ungrouped = rm.getString(ResourceIDs.STR_UNGROUPED);
	public static String NETWORKS = rm.getString(ResourceIDs.STR_NETWORKS);
	public static String SERVICES = rm.getString(ResourceIDs.STR_SERVICES);

	static String highLightString = rm.getString(ResourceIDs.STR_HIGHLIGHTS);

	protected UIAccordion accordion = null;

	public static UILabel moveLabel = new UILabel(rm
			.getString(ResourceIDs.STR_MOVE));
	public static UILabel openLabel = new UILabel(rm
			.getString(ResourceIDs.STR_OPEN));

	/*
	 * The name of the group
	 */
	public String name;

	public boolean virtualGroup = false;

	public static Hashtable uiGroups = new Hashtable();

	static Vector groupsPosition = new Vector();

	/*
	 * Used to know that any of the painted groups is moving; -1 means noone
	 */
	public static UIGroup movingGroup = null;

	int movingColor = UIUtils.colorize(UIConfig.bg_color, -50);

	int normalFontColor = this.getFg_color();

	int movingFontColor = UIUtils.colorize(UIConfig.bg_color, -25);

	private static UIMenu groupActionsMenu = null;

	public static UIMenu groupActionsLabel = null;

	static {
		loadGroups();
		groupActionsMenu = new UIMenu("");
		groupActionsLabel = new UIMenu(rm.getString(ResourceIDs.STR_OPTIONS));
		groupActionsMenu.append(groupActionsLabel);
	}

	private static void loadGroups() {
		groupsPosition.removeAllElements();
		byte[] gps = Config.getInstance().getData(
				Config.GROUPS_POSITION.getBytes());
		if (gps != null && gps.length > 0) {
			Element gpe = BProcessor.parse(gps);
			Element[] groups = gpe.getChildrenByName(null, "group");
			for (int i = 0; i < groups.length; i++) {
				Element ithGroup = groups[i];
				String name = ithGroup.getChildByName(null, "name").getText();
				if (groupsPosition.contains(name) == false) {
					groupsPosition.addElement(name);
				}
			}
		}
	}

	protected UIGroup(String groupName, UIAccordion accordion, int constraint) {
		super(groupName);
		this.groupConstraint = constraint;
		if (groupName == MUC.GROUP_CHATS)
			this.virtualGroup = true;
		this.name = groupName;
		this.accordion = accordion;
		this.setSubmenu(groupActionsMenu);

		UIFont xFont = UIConfig.font_body;
		UIFont lfont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		setFont(lfont);
		Vector newGroup = new Vector();
		uiGroups.put(groupName, this);

		if (groupsPosition.contains(groupName) == false) {
			if (constraint == UIGroup.END)
				groupsPosition.addElement(groupName);
			else if (constraint == UIGroup.BEGINNING)
				groupsPosition.insertElementAt(groupName, 0);
			else if (constraint == UIGroup.MIDDLE) {
				int foundPosition = groupsPosition.size();
				for (int i = 0; i < groupsPosition.size(); i++) {
					UIGroup ithGroup = UIGroup.getGroup((String) groupsPosition
							.elementAt(i), accordion, false);
					if (ithGroup != null
							&& ithGroup.groupConstraint > UIGroup.BEGINNING) {
						foundPosition = i;
						break;
					}
				}
				groupsPosition.insertElementAt(groupName, foundPosition);
			}
			saveGroups();
		}

		//		if (groupName.equals(highLightString)) {
		//			accordion.insertItem(this, 0, newGroup);
		//		} else {
		accordion.addItem(this, newGroup);
		orderGroups();
		//		}

		updateColors();
	}

	public int getHeight(UIGraphics g) {
		this.height = super.getHeight(g);
		if (this.height < UIRosterItem.minHeight
				&& UICanvas.getInstance().hasPointerEvents()) {
			this.height = UIRosterItem.minHeight;
		}
		return this.height;
	}

	protected void paint(UIGraphics g, int w, int h) {
		int oldFontColor = this.getFg_color();
		UIFont oldFont = this.getFont();
		if (isSelected()) {
			UIFont lFont = UIFont.getFont(oldFont.getFace(), UIFont.STYLE_BOLD,
					oldFont.getSize());
			this.setFont(lFont);
			this.setFg_color(UIConfig.menu_title);
		}
		super.paint(g, w, h);
		this.setFg_color(oldFontColor);
		this.setFont(oldFont);
	}

	/**
	 * Update the colors for virtual groups
	 */
	public void updateColors() {
		if (this.virtualGroup == true) {
			int oldBgColor = this.getBg_color();
			int oldGradientColor = this.getGradientColor();
			this.setBg_color(UIUtils.colorize(oldBgColor, -13));
			this.setGradientColor(UIUtils.colorize(oldGradientColor, -13));
		}
	}

	private static void saveGroups() {
		Config cfg = Config.getInstance();
		Element el = new Element("groups", "groups");
		Enumeration en = groupsPosition.elements();
		while (en.hasMoreElements()) {
			String ithName = (String) en.nextElement();
			Element group = el.addElement(null, "group");
			group.addElement(null, "name").addText(ithName);
			// #mdebug 
			Logger.log("Saving group: " + ithName);
			// #enddebug
		}
		cfg.setData(Config.GROUPS_POSITION.getBytes(), BProcessor.toBinary(el));
		cfg.saveToStorage();
	}

	private void orderGroups() {
		boolean changed = true;
		while (changed == true) {
			changed = false;
			UIItem[] labels = accordion.getItemLabels();
			for (int i = 0; i < labels.length - 1; i++) {
				int firstPosition = groupsPosition
						.indexOf(((UIGroup) labels[i]).name);
				int secondPosition = groupsPosition
						.indexOf(((UIGroup) labels[i + 1]).name);
				if (firstPosition > secondPosition) {
					accordion.swap(i, i + 1);
					changed = true;
				}
			}
		}
		//		if (changed) {
		//			orderGroups();
		//		}
	}

	public void startMoving() {
		//moving = true;
		accordion.openLabel(this);
		accordion.closeLabel(this);
		UIGroup.movingGroup = this;
		int[] backupColor = new int[] { this.getGradientColor(),
				this.getGradientSelectedColor(), this.getSelectedColor(),
				this.getFg_color(), };
		this.setStatus(backupColor);
		setGradientColor(movingColor);
		setGradientSelectedColor(movingColor);
		setSelectedColor(movingColor);
		setFg_color(movingFontColor);
		//accordion.closeLabel(accordion.getOpenedLabel());
	}

	public void stopMoving() {
		//moving = false;
		this.setDirty(true);
		UIGroup.movingGroup = null;
		int[] backupColor = (int[]) this.getStatus();
		setGradientColor(backupColor[0]);
		setGradientSelectedColor(backupColor[1]);
		setSelectedColor(backupColor[2]);
		setFg_color(backupColor[3]);
		saveGroups();
	}

	public boolean keyPressed(int key) {
		if (UIGroup.movingGroup == null)
			return super.keyPressed(key);
		if (UIGroup.movingGroup != null && UIGroup.movingGroup != this) {
			UIItem[] labels = accordion.getItemLabels();
			int myIndex = 0;
			int movingIndex = 0;
			boolean oldFreezed = this.getScreen().isFreezed();
			this.getScreen().setFreezed(true);
			for (int i = 0; i < labels.length; i++) {
				if (labels[i] == this)
					myIndex = i;
				else if (labels[i] == UIGroup.movingGroup)
					movingIndex = i;
			}
			moveGroups(this.accordion, movingIndex, myIndex, false);
			UIGroup.movingGroup.stopMoving();
			// little hack to "clean" selections
			((UILayout) this.getContainer()).setSelectedIndex(-1);
			((UILayout) ((UILayout) this.getContainer()).getContainer())
					.setSelectedIndex(-1);
			this.setSelected(false);
			//this.accordion.setSelectedIndex(myIndex);
			this.getScreen().setFreezed(oldFreezed);
			this.askRepaint();
			return true;
		}
		int ga = UICanvas.getInstance().getGameAction(key);
		int index = 0;
		UIItem[] labels = null;
		switch (ga) {
		case UICanvas.UP:
			labels = accordion.getItemLabels();
			for (int i = 0; i < labels.length; i++) {
				if (labels[i] == this) {
					index = i;
					break;
				}
			}
			if (index > 0)
				moveGroups(this.accordion, index, index - 1, true);
			break;

		case UICanvas.DOWN:
			labels = accordion.getItemLabels();
			for (int i = 0; i < labels.length; i++) {
				if (labels[i] == this) {
					index = i;
					break;
				}
			}
			if (index < accordion.getItems().size() - 1)
				moveGroups(this.accordion, index, index + 1, true);
			break;

		case UICanvas.FIRE:
			this.stopMoving();
			break;

		default:
			break;
		}

		return true;
	}

	/**
	 * @param accordion
	 * @param firstIndex
	 * @param secondIndex
	 */
	private static void moveGroups(UIAccordion accordion, int firstIndex,
			int secondIndex, boolean swap) {
		UIItem[] labels = accordion.getItemLabels();
		UIGroup firstLabel = (UIGroup) labels[firstIndex];
		UIGroup secondLabel = (UIGroup) labels[secondIndex];

		int firstPosition = groupsPosition.indexOf(firstLabel.name);
		int secondPosition = groupsPosition.indexOf(secondLabel.name);
		groupsPosition.setElementAt(secondLabel.name, firstPosition);
		groupsPosition.setElementAt(firstLabel.name, secondPosition);

		if (swap)
			accordion.swap(firstIndex, secondIndex);
		else {
			accordion.move(firstIndex, secondIndex);
			// now it can be another one!!! set it to dirty
			for (int i = firstIndex; i <= secondIndex; i++) {
				UIItem item = labels[i];
				item.setDirty(true);
			}
		}
	}

	public UIMenu openGroupMenu() {
		UIMenu groupOptionsMenu = UIUtils.easyMenu(rm
				.getString(ResourceIDs.STR_GROUP), 10, this.getSubmenu()
				.getAbsoluteY(), UICanvas.getInstance().getWidth() - 20,
				moveLabel);
		groupOptionsMenu.append(openLabel);
		RosterScreen.getInstance().addPopup(groupOptionsMenu);
		return groupOptionsMenu;
	}

	public static UIGroup getGroup(String group, UIAccordion accordion,
			boolean allocate) {
		// some groups have peculiar handling 
		// - search
		// - highlight
		// - ungrouped
		// - mucs

		// In the roster the contacts without group
		// are in the "ungrouped" group with label == Roster.unGroupedCode 
		if (group.equals(Roster.unGroupedCode))
			group = UIGroup.ungrouped;

		Hashtable tempGroups = uiGroups;
		UIGroup groupLabel = (UIGroup) tempGroups.get(group);
		if (groupLabel == null && allocate == true) {
			if (group == highLightString)
				groupLabel = new UIContactGroup(highLightString, accordion,
						UIGroup.BEGINNING);
			else if (group == MUC.GROUP_CHATS)
				groupLabel = new UIContactGroup(group, accordion, UIGroup.END);
			else if (group == NETWORKS)
				groupLabel = new UIGatewayGroup(accordion);
			else
				groupLabel = new UIContactGroup(group, accordion,
						UIGroup.MIDDLE);
		}
		return groupLabel;
	}
}
