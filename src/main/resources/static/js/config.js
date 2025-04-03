const isLocal = window.location.hostname === "localhost";

const BASE_URL = isLocal ? "http://localhost:8080" : "https://viveroviveverde.site";

export default BASE_URL;
