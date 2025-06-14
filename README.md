# Optimisation de Problèmes Pseudo-Booléens Quadratiques (QUBO)

L'énoncé est disponible dans le fichier PDF **`projet_m1_optimisation_2025.pdf`**.

## 📚 Description

Ce projet porte sur l'**optimisation de problèmes Quadratic Unconstrained Binary Optimization (QUBO)**. L'objectif est de développer et comparer différentes approches d'optimisation classiques et hybrides pour résoudre des problèmes QUBO, avec un accent particulier sur les performances par rapport aux solveurs quantiques émergents.

## ⚙️ Fonctionnalités principales

- Implémentation modulaire en **Java**
- Méthodologie d'évaluation incrémentale pour accélérer les calculs
- Algorithmes inclus :
  - Recherche Aléatoire
  - Recherche Locale Itérée (ILS)
  - Recherche Tabou (TS)
  - Recuit Simulé (SA)
  - Algorithme Génétique (GA)
  - Descente de Gradient (GD)
  - **HybridSA_GDOptimiser** (approche hybride SA + GD)

## 🧪 Méthodologie de test

- Instances QUBO testées : **990 à 999**
- 30 exécutions par algorithme et par instance
- Résultats enregistrés sous forme de fichiers CSV

## 📈 Résultats

- **Meilleur performeur global : Recuit Simulé (SA)**
- L'approche hybride a montré un potentiel intéressant sur certaines instances mais reste incohérente.
- Le Gradient Descent seul n'a pas produit de résultats satisfaisants.

## 🚀 Améliorations futures

- Exploration d'autres répartitions temporelles SA-GD
- Ajout de contraintes pour améliorer l'adéquation binaire des solutions continues
- Extension des tests à l'ensemble complet des instances

## 👨‍💻 Auteur

**Fellah Mohammed Nassim**  
Version finale : avril 2025

---

📎 Voir le fichier `Projet_Optimisation_de_problèmes_pseudo_booléen_quadratique_QUBO.pdf` pour le rapport complet.

