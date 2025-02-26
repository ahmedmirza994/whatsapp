module.exports = {
	// Angular formatting
	"whatsapp-web/src/**/*.{ts,html,scss,css,json}": ["prettier --write"],

	// Java formatting - using Google Java Format
	"whatsapp-backend/src/**/*.java": [
		"java -jar ./tools/google-java-format.jar --replace",
	],
};
