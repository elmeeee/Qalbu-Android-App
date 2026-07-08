#!/bin/bash

# Exit on any error
set -e

# --- Colors ---
RESET='\033[0m'
BOLD='\033[1m'
RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
CYAN='\033[1;36m'
MAGENTA='\033[1;35m'

# --- ASCII Banner ---
clear
echo -e "${CYAN}${BOLD}"
cat << "EOF"
   _____             __ 
  / ___/____ _____ _/ /_
  \__ \/ __ `/ __ `/ __/
 ___/ / /_/ / /_/ / /_  
/____/\__,_/\__,_/\__/  
                        
EOF
echo -e "${RESET}"
echo -e "${MAGENTA}${BOLD}=================================================================${RESET}"
echo -e "${YELLOW}${BOLD}     Sāat Production Build Assistant${RESET}"
echo -e "${MAGENTA}${BOLD}=================================================================${RESET}"
echo -e "${BLUE}     © $(date +%Y) Elmee. All Rights Reserved.${RESET}"
echo -e "${MAGENTA}${BOLD}=================================================================${RESET}"
echo ""

echo -e "${CYAN}[*] Step 1: Enforcing pristine build environment...${RESET}"
echo -e "${YELLOW}🧹 Cleaning the project...${RESET}"
./gradlew clean
echo -e "${GREEN}Clean finished successfully!${RESET}"
echo ""

echo -e "${CYAN}[*] Step 2: Select build target${RESET}"
echo -e "  ${BOLD}1)${RESET} AAB (Android App Bundle) ${MAGENTA}[Play Store Release]${RESET}"
echo -e "  ${BOLD}2)${RESET} APK (Android Package)    ${BLUE}[Direct Install / QA]${RESET}"
echo -e "  ${BOLD}3)${RESET} Both AAB and APK         ${GREEN}[Ultimate Package]${RESET}"
echo -e "  ${BOLD}4)${RESET} Exit"
echo ""
read -p "$(echo -e ${BOLD}Enter your choice [1-4]: ${RESET})" BUILD_CHOICE

echo ""

case $BUILD_CHOICE in
    1)
        echo -e "${YELLOW}Building App Bundle (AAB)...${RESET}"
        ./gradlew bundleRelease
        echo ""
        echo -e "${GREEN}${BOLD}Build Complete!${RESET}"
        echo -e " ${CYAN}AAB Location: app/build/outputs/bundle/release/app-release.aab${RESET}"
        ;;
    2)
        echo -e "${YELLOW}Building Android Package (APK)...${RESET}"
        ./gradlew assembleRelease
        echo ""
        echo -e "${GREEN}${BOLD}Build Complete!${RESET}"
        echo -e " ${CYAN}APK Location: app/build/outputs/apk/release/app-release.apk${RESET}"
        ;;
    3)
        echo -e "${YELLOW}Building both AAB and APK...${RESET}"
        ./gradlew bundleRelease assembleRelease
        echo ""
        echo -e "${GREEN}${BOLD}Build Complete!${RESET}"
        echo -e " ${CYAN}AAB Location: app/build/outputs/bundle/release/app-release.aab${RESET}"
        echo -e " ${CYAN}APK Location: app/build/outputs/apk/release/app-release.apk${RESET}"
        ;;
    4)
        echo -e "${RED}Exiting without building.${RESET}"
        exit 0
        ;;
    *)
        echo -e "${RED}${BOLD}Invalid choice. Exiting.${RESET}"
        exit 1
        ;;
esac

echo ""
echo -e "${MAGENTA}${BOLD}=================================================================${RESET}"
echo -e "${YELLOW}     Ready for Deployment!${RESET}"
echo -e "${MAGENTA}${BOLD}=================================================================${RESET}"
echo ""
