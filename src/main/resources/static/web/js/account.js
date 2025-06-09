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

    let isUpdatingPhone = false;

    $('#submitPhoneBtn').click(function(e) {
        e.preventDefault();
        if (isUpdatingPhone) {
            const verificationCode = $('#otpInput').val().trim();
            const newPhone = $('#newPhoneInput').val().trim();

            if (verificationCode === '') {
                $('#success-message').text('Vui lòng nhập mã OTP').fadeIn().delay(3000).fadeOut();
                return;
            }

            $.ajax({
                url: '/account/update-phone',
                type: 'POST',
                data: {
                    phone: newPhone,
                    verificationCode: verificationCode
                },
                success: function(response) {
                    $('#success-message').text(response).fadeIn().delay(3000).fadeOut();
                    if (response === "Cập nhật thành công") {
                        $('input[name="phone"]').val(newPhone);
                        isUpdatingPhone = false;
                        $('#updatePhoneModal').modal('hide');
                        $('#newPhoneInput').val('');
                        $('#otpInput').val('');
                        $('#otpInputContainer').hide();
                        $('#submitPhoneBtn').text('Gửi mã OTP');
                    }
                },
                error: function(xhr) {
                    $('#success-message').text(xhr.responseText || 'Cập nhật thất bại').fadeIn().delay(3000).fadeOut();
                    errorToast.show();
                }
            });
        } else {
            // Gửi mã OTP
            const newPhone = $('#newPhoneInput').val().trim();
            if (newPhone && /^0\d{9}$/.test(newPhone)) {
                $.ajax({
                    url: '/send-verification-code',
                    type: 'POST',
                    data: { phone: newPhone },
                    success: function(response) {
                        $('#success-message').text(response).fadeIn().delay(3000).fadeOut();
                        isUpdatingPhone = true;
                        $('#otpInputContainer').show();
                        $('#submitPhoneBtn').text('Xác nhận');
                    },
                    error: function(xhr) {
                        $('#success-message').text(xhr.responseText || 'Không thể gửi mã xác thực').fadeIn().delay(3000).fadeOut();
                        errorToast.show();
                    }
                });
            } else {
                $('#success-message').text('Số điện thoại không hợp lệ').fadeIn().delay(3000).fadeOut();
            }
        }
    });

    $('#updatePhoneModal').on('hidden.bs.modal', function() {
        isUpdatingPhone = false;
        $('#newPhoneInput').val('');
        $('#otpInput').val('');
        $('#otpInputContainer').hide();
        $('#submitPhoneBtn').text('Gửi mã OTP');
    });

    $('#address').on('input', function() {
        const currentAddress = $(this).val().trim();
        console.log("Debug - Current address on input: " + currentAddress);
    });
});
$(document).ready(function () {
    $('.cancel-order-btn').click(function () {
        const orderId = $(this).data('order-id');
        $('#cancelOrderId').text(orderId);
        $('#confirmCancelBtn').data('order-id', orderId);
    });

    $('#confirmCancelBtn').click(function () {
        const orderId = $(this).data('order-id');
        $.ajax({
            url: '/account/cancel-order/' + orderId,
            type: 'POST',
            headers: {
                'X-CSRF-TOKEN': $('input[name="_csrf"]').val()
            },
            success: function (response) {
                $('#cancelOrderModal').modal('hide');
                // Hiển thị toast thành công
                const successToast = new bootstrap.Toast(document.getElementById('successToast'));
                $('#successToast .toast-body').text('Hủy đơn hàng thành công');
                successToast.show();
                setTimeout(() => location.reload(), 3000);
            },
            error: function (xhr) {
                $('#cancelOrderModal').modal('hide');
                const errorToast = new bootstrap.Toast(document.getElementById('errorToast'));
                $('#errorToast .toast-body').text(xhr.responseText || 'Có lỗi xảy ra khi hủy đơn hàng');
                errorToast.show();
            }
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const rowsPerPage = 5;
    const tableBody = document.querySelector("#orderTable tbody");
    const rows = Array.from(tableBody.querySelectorAll("tr"));

    // Sắp xếp giảm dần theo Order ID
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
        updatePagination(page);
    }

    function updatePagination(currentPage) {
        pagination.innerHTML = "";

        const firstLi = document.createElement("li");
        firstLi.classList.add("page-item");
        if (currentPage === 0) firstLi.classList.add("disabled");
        const firstA = document.createElement("a");
        firstA.classList.add("page-link");
        firstA.textContent = "<<";
        firstA.href = "#";
        firstA.addEventListener("click", function (event) {
            event.preventDefault();
            if (currentPage > 0) displayPage(0);
        });
        firstLi.appendChild(firstA);
        pagination.appendChild(firstLi);

        const prevLi = document.createElement("li");
        prevLi.classList.add("page-item");
        if (currentPage === 0) prevLi.classList.add("disabled");
        const prevA = document.createElement("a");
        prevA.classList.add("page-link");
        prevA.textContent = "<";
        prevA.href = "#";
        prevA.addEventListener("click", function (event) {
            event.preventDefault();
            if (currentPage > 0) displayPage(currentPage - 1);
        });
        prevLi.appendChild(prevA);
        pagination.appendChild(prevLi);

        const visiblePages = 5;
        let startPage = Math.max(0, currentPage - Math.floor(visiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);

        if (endPage - startPage + 1 < visiblePages) {
            startPage = Math.max(0, endPage - visiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            const li = document.createElement("li");
            li.classList.add("page-item");
            if (i === currentPage) li.classList.add("active");

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

        const nextLi = document.createElement("li");
        nextLi.classList.add("page-item");
        if (currentPage === totalPages - 1) nextLi.classList.add("disabled");
        const nextA = document.createElement("a");
        nextA.classList.add("page-link");
        nextA.textContent = ">";
        nextA.href = "#";
        nextA.addEventListener("click", function (event) {
            event.preventDefault();
            if (currentPage < totalPages - 1) displayPage(currentPage + 1);
        });
        nextLi.appendChild(nextA);
        pagination.appendChild(nextLi);

        const lastLi = document.createElement("li");
        lastLi.classList.add("page-item");
        if (currentPage === totalPages - 1) lastLi.classList.add("disabled");
        const lastA = document.createElement("a");
        lastA.classList.add("page-link");
        lastA.textContent = ">>";
        lastA.href = "#";
        lastA.addEventListener("click", function (event) {
            event.preventDefault();
            if (currentPage < totalPages - 1) displayPage(totalPages - 1);
        });
        lastLi.appendChild(lastA);
        pagination.appendChild(lastLi);
    }

    displayPage(0);
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