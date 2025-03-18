const isLocal = window.location.hostname === "localhost";

const BASE_URL = isLocal ? "http://localhost:8080" : "http://149.50.139.89:8080";

export default BASE_URL;
