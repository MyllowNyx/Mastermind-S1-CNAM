package Views;

import Controllers.AllPartiesByIdController;
import Controllers.JeuController;
import Modele.Joueur;
import Modele.Partie;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class AllPartiesByIdView extends JFrame {

    private JTable partiesTable;
    private JLabel playerNameLabel = new JLabel();
    private JLabel titleLabel = new JLabel();
    private Object[][] tableauParties;

    private JPanel listeJoueursPanel;
    private JPanel boutonsPanel;
    private JButton boutonRetourBtn;
    private JButton boutonNouvPartieBtn;

    private AllPartiesByIdController AllPartiesByIdController;

    private Joueur joueur;
    private Partie partie;

    public AllPartiesByIdView(Joueur joueur, Partie partie) {
        this.joueur = joueur;
        this.partie = partie;
    }

    public void setController(AllPartiesByIdController controller) {
        this.AllPartiesByIdController = controller;
    }

    public void initializeView() {
        tableauParties = AllPartiesByIdController.recupererPartiesJoueur();

        // Créer un panel pour le titre
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridBagLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Ajoute des marges autour

        // Configuration du titre
        titleLabel.setText("Parties de " + joueur.getNom());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centrer le texte

        // Ajouter le titre au panel
        titlePanel.add(titleLabel);

        // Ajouter le panel à la fenêtre
        add(titlePanel, BorderLayout.NORTH);

        playerNameLabel.setText("Nom du Joueur : " + joueur.getNom());

        afficherPartiesJoueur(tableauParties);

        // Panel pour les boutons
        JPanel boutonsPanel = new JPanel();
        boutonsPanel.setLayout(new BorderLayout());

        // Bouton "Nouvelle partie"
        JButton boutonNouvPartieBtn = createNewPartieBtn();
        boutonsPanel.add(boutonNouvPartieBtn, BorderLayout.CENTER);

        // Utiliser le bouton défini au niveau de la classe
        boutonRetourBtn = new JButton("Retour au menu"); // Initialisez ici si ce n'est pas encore fait
        boutonsPanel.add(boutonRetourBtn, BorderLayout.EAST);

        add(boutonsPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }


    public void afficherPartiesJoueur(Object[][] tableauParties) {
        String[] columnNames = {"ID Partie", "État", "Coups Max", "Action"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Seule la colonne des boutons est éditable pour activer le clic
            }
        };

        for (Object[] partie : tableauParties) {
            int idPartie = (int) partie[1];
            int etat = (int) partie[5];
            int nbcoupsPartie = (int) partie[2];
            String etatPartie = obtenirEtatPartie(etat);

            tableModel.addRow(new Object[]{idPartie, etatPartie, nbcoupsPartie, "Voir la partie"});
        }

        partiesTable = new JTable(tableModel);
        partiesTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        partiesTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox())); // Utiliser un JCheckBox pour activer le bouton sans édition

        // Centrer les cellules de données
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < partiesTable.getColumnCount() - 1; i++) { // Éviter la dernière colonne (boutons)
            partiesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(partiesTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private String obtenirEtatPartie(int etat) {
        switch (etat) {
            case 0: return "En attente";
            case 1: return "Gagné";
            case 2: return "Perdu";
            default: return "Inconnu";
        }
    }

    // Classe pour afficher le bouton sans permettre l'édition de son texte
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Voir la partie");
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Classe pour gérer l'action du bouton sans permettre de modifier le texte
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Voir la partie");
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Arrêter l'édition pour que le bouton réagisse
                    AllPartiesByIdController.afficherPartie((int) tableauParties[currentRow][1]); // ID de la partie
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; // Récupérer l'index de la ligne actuelle pour l'action
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            clicked = false;
            return "Voir la partie";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    public JButton getBoutonRetourBtn() {
        return boutonRetourBtn;
    }

    private JButton createNewPartieBtn() {
        JButton createNewPartieBtn = new JButton("Nouvelle partie");
        createNewPartieBtn.addActionListener(_ -> {
            HashMap<String, Object> formInput = new HashMap<>();
            formInput.put("lengthCoup", 0);
            formInput.put("maxColors", 0);
            formInput.put("maxCoup", 0);

            NewPartieForm newPartieForm = new NewPartieForm(formInput);
            HashMap<String, Object> formResult = newPartieForm.getFormResult();

            if (!formResult.isEmpty()) {
                System.out.print(formInput);
                partie = new Partie();
                partie.initiateNouvellePartie((int) formResult.get("lengthCoup"),(int) formResult.get("maxCoup"),(int) formResult.get("maxColors"),
                        joueur.getId());
                this.setVisible(false);
                JeuView vueJeu = new JeuView(partie, joueur);
                JeuController jeu = JeuController.getInstance();
                jeu.setPartieEnCours(partie);
                this.dispose();
            }
        });
        return createNewPartieBtn;
    }
}
