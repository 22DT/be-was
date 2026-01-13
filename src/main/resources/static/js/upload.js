const input = document.getElementById("image-input");
const preview = document.getElementById("preview");
const form = document.querySelector("form");

let objectUrl = null;
let pathInput = null; // 나중에 생성

input.addEventListener("change", async () => {
  const file = input.files[0];
  if (!file) return;

  //  기존 미리보기 URL 해제 (메모리 누수 방지)
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
  }

  //  1. 클라이언트 미리보기
  objectUrl = URL.createObjectURL(file);
  preview.src = objectUrl;
  preview.style.display = "block";

  //  2. 서버 업로드
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("/files", {
    method: "POST",
    body: formData
  });

  if (!response.ok) {
    alert("업로드 실패");
    return;
  }

  const result = await response.json();

  // 여기서 hidden input 생성
  if (!pathInput) {
    pathInput = document.createElement("input");
    pathInput.type = "hidden";
    pathInput.name = "imagePath";
    form.appendChild(pathInput);
  }

  pathInput.value = result.path;
});
