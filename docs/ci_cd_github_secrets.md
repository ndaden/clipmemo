# Configuration des GitHub Secrets & CI/CD ClipMemo (Modèle Quizzy)

Ce guide détaille la configuration des **GitHub Secrets** et de la CI/CD pour ClipMemo, calquée sur le modèle du projet **Quizzy** :
1. Déploiement direct sur votre cluster **k3s** via `KUBECONFIG` (base64) et `azure/setup-kubectl@v3` (aucun accès SSH nécessaire).
2. Authentification du registre d'images GHCR via `GHCR_PULL_TOKEN` (`ghcr-pull-secret`).
3. Namespace dédié `clipmemo` et routage Traefik natif (`IngressRoute`) sur `clipmemo.dnabil.ovh` avec Let's Encrypt.
4. Compilation automatisée de l'application **Android (APK)** avec les identifiants injectés.

---

## 1. Liste des Secrets à configurer

Rendez-vous sur votre dépôt GitHub :  
👉 **Settings** > **Secrets and variables** > **Actions** > **New repository secret**

| Nom du Secret | Description | Statut / Provenance |
|---|---|---|
| `KUBECONFIG` | Contenu de votre fichier kubeconfig encodé en base64 (`base64 -w 0 ~/.kube/config`) | **Identique à Quizzy** |
| `GHCR_PULL_TOKEN` | Token d'accès personnel GitHub (PAT) avec permission `read:packages` | **Identique à Quizzy** |
| `GEMINI_API_KEY` | Clé API Google AI Studio pour le backend Fastify | À renseigner pour ClipMemo |
| `APP_SECRET_KEY` | Clé secrète partagée app Android <-> backend (`X-App-Key`) | À renseigner pour ClipMemo |
| `BACKEND_BASE_URL` | *(Optionnel)* URL publique HTTPS du backend (valeur par défaut : `https://clipmemo.dnabil.ovh`) | Optionnel |

---

## 2. Rappel : Comment générer la valeur de `KUBECONFIG` (si besoin)

Si vous devez réexporter le kubeconfig de votre cluster k3s :

```bash
# Sur votre machine ou votre VPS k3s :
cat /etc/rancher/k3s/k3s.yaml | sed "s/127.0.0.1/VOTRE_IP_VPS_OU_DOMAINE/g" | base64 -w 0
```
Collez la chaîne base64 obtenue dans le secret `KUBECONFIG`. (Si vous l'avez déjà configuré pour Quizzy au niveau organisation ou compte, vous pouvez simplement le réutiliser).

---

## 3. Fonctionnement des Workflows CI/CD

### A. Déploiement Kubernetes API (`.github/workflows/deploy.yml`)
- **Déclenchement :**
  - Automatique lors d'un `git push` sur `main` qui modifie `backend/**`.
  - Manuel depuis l'onglet **Actions** de GitHub (*Build and Deploy ClipMemo API to Kubernetes* > *Run workflow*).
- **Actions réalisées :**
  1. Construit l'image Docker du backend et la publie sur GitHub Packages :
     - `ghcr.io/ndaden/clipmemo-api:latest`
     - `ghcr.io/ndaden/clipmemo-api:<git-sha>`
  2. Configure `kubectl` avec le secret `KUBECONFIG`.
  3. Crée le namespace `clipmemo` (s'il n'existe pas).
  4. Crée / met à jour le secret de tirage d'images `ghcr-pull-secret` avec `GHCR_PULL_TOKEN`.
  5. Crée / met à jour le secret Kubernetes `clipmemo-secrets` avec vos clés `GEMINI_API_KEY` et `APP_SECRET_KEY`.
  6. Met à jour l'image tag dans `backend/k8s/03-api-deployment.yaml` avec le SHA du commit en cours.
  7. Applique les manifestes Kubernetes dans l'ordre :
     - `03-api-deployment.yaml` (Deployment)
     - `04-api-service.yaml` (Service ClusterIP)
     - `05-ingressroute.yaml` (Traefik IngressRoute avec SSL cert-manager)
  8. Déclenche le redémarrage et attend la validation du rollout (`kubectl rollout status deployment clipmemo-api -n clipmemo`).

### B. Build Android (`.github/workflows/build-android.yml`)
- **Déclenchement :**
  - Automatique lors d'un `git push` sur `main` qui modifie `app/**`.
  - Manuel depuis l'onglet **Actions** de GitHub (*Build Android APK* > *Run workflow*).
- **Actions réalisées :**
  1. Initialise Java 21 et Gradle.
  2. Injecte `BACKEND_BASE_URL` (`https://clipmemo.dnabil.ovh`) et `APP_SECRET_KEY` directement dans l'APK.
  3. Exécute les tests unitaires (`./gradlew test`).
  4. Compile les APKs Debug et Release.
  5. Sauvegarde les APKs dans les artefacts de téléchargement GitHub Actions.

---

## 4. Vérification après déploiement

Sur votre VPS :
```bash
# Vérifier les pods dans le namespace clipmemo
kubectl get pods -n clipmemo

# Vérifier les logs de l'API
kubectl logs -n clipmemo deployment/clipmemo-api --tail=50

# Tester le endpoint de health
curl https://clipmemo.dnabil.ovh/health
# {"status":"ok","timestamp":"..."}
```
