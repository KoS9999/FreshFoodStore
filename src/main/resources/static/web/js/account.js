$(document).ready(function() {
    const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));
    const addressToast = new bootstrap.Toast(document.getElementById('addressToast'));
    $('#update-account-form').submit(function(e) {
        e.preventDefault();
        const addressInput = $('#address').val().trim();

        if (isValidAddress && addressInput !== '') {
            $.ajax({
                url: '/account/update',
                type: 'POST',
                data: $(this).serialize(),
                success: function(response) {
                    $('#success-message').text("Thông tin của bạn đã được cập nhật thành công!").fadeIn().delay(3000).fadeOut();
                },
                error: function(xhr) {
                    errorToast.show();
                }
            });
        } else {
            addressToast.show();
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const rowsPerPage = 5;
    const tableBody = document.querySelector("#orderTable tbody");
    const rows = Array.from(tableBody.querySelectorAll("tr"));

    rows.sort((a, b) => {
        const idA = parseInt(a.children[0].textContent.trim());
        const idB = parseInt(b.children[0].textContent.trim());
        return idB - idA;
    });

    rows.forEach(row => tableBody.appendChild(row));

    const pagination = document.getElementById("pagination");
    const totalPages = Math.ceil(rows.length / rowsPerPage);

    function displayPage(page) {
        rows.forEach((row) => {
            row.style.display = "none";
        });
        const start = page * rowsPerPage;
        const end = start + rowsPerPage;
        rows.slice(start, end).forEach(row => {
            row.style.display = "table-row";
        });
        document.querySelectorAll(".pagination .page-item").forEach((item, index) => {
            if (index === page) {
                item.classList.add("active");
            } else {
                item.classList.remove("active");
            }
        });
    }
    function createPagination() {
        for (let i = 0; i < totalPages; i++) {
            const li = document.createElement("li");
            li.classList.add("page-item");
            if (i === 0) li.classList.add("active");

            const a = document.createElement("a");
            a.classList.add("page-link");
            a.textContent = i + 1;
            a.href = "#";
            a.dataset.page = i;

            a.addEventListener("click", function (event) {
                event.preventDefault();
                displayPage(parseInt(a.dataset.page));
            });

            li.appendChild(a);
            pagination.appendChild(li);
        }
    }
    displayPage(0);
    createPagination();
});
document.getElementById('checkInBtn').addEventListener('click', function () {
    fetch('/account/check-in')
        .then(response => response.json())
        .then(data => {
            const messageDiv = document.getElementById('checkin-message');
            messageDiv.innerHTML = '';

            const alert = document.createElement('div');
            alert.className = data.success ? 'alert alert-success mb-0 px-3 py-2' : 'alert alert-danger mb-0 px-3 py-2';
            alert.style.lineHeight = '1.6';
            alert.style.minHeight = '42px';
            alert.style.display = 'flex';
            alert.style.alignItems = 'center';
            alert.style.margin = '0';
            alert.innerText = data.message;

            messageDiv.appendChild(alert);
            setTimeout(function() {
                alert.style.transition = "opacity 0.3s ease-out";
                alert.style.opacity = 0;
                setTimeout(function() {
                    alert.remove();
                }, 300);
            }, 3000);

            if (data.success) {
                const totalSpan = document.querySelector('[th\\:text="${totalCheckIn}"]');
                if (totalSpan) {
                    let current = parseInt(totalSpan.innerText);
                    totalSpan.innerText = current + 1;
                }

                const list = document.querySelector('.list-group');
                const newItem = document.createElement('li');
                newItem.className = 'list-group-item';
                const today = new Date().toLocaleDateString('vi-VN');
                newItem.innerHTML = `<i class="fas fa-check-circle text-success"></i> ${today} - +1000 điểm`;
                list.prepend(newItem);
            }
        });
});
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
tooltipTriggerList.map(el => new bootstrap.Tooltip(el));