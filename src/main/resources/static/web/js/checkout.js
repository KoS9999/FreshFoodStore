// Xử lý submit form COD
document.getElementById("codForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = document.getElementById("createCODBtn");
    submitButton.disabled = true;

    const totalPrice = parseFloat(document.getElementById('totalPriceInput').value);
    const rows = document.querySelectorAll('#itemsTable tbody tr');
    const items = [];

    rows.forEach((row) => {
        const itemIdElem = row.querySelector('.itemid');
        const nameElem = row.querySelector('td:nth-child(2) span');
        const quantityElem = row.querySelector('td:nth-child(3) span');
        const unitPriceElem = row.querySelector('td:nth-child(4) span');

        if (itemIdElem && nameElem && quantityElem && unitPriceElem) {
            const item = {
                itemid: parseInt(itemIdElem.textContent.trim(), 10),
                itename: nameElem.textContent.trim(),
                itemprice: parseInt(unitPriceElem.textContent.trim(), 10) * 1000,
                itemquantity: parseInt(quantityElem.textContent.trim(), 10),
            };
            items.push(item);
        }
    });

    const voucherCode = document.getElementById('voucherCodeInput').value;
    const usedPoints = document.getElementById('usedPointsHiddenInput').value;
    const shippingCost = parseFloat(document.getElementById('shippingCost').textContent);
    const distanceKm = parseFloat(document.getElementById('distanceKm').textContent);
    const estimatedTime = document.getElementById('estimatedTime').textContent;

    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
        shippingCost: (shippingCost * 1000),
        estimatedTime: estimatedTime,
        distanceKm: distanceKm.toFixed(2),
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    const requestData = {
        app_user: document.querySelector('input[name="app_user"]').value,
        totalPrice: totalPrice.toFixed(2),
        items: JSON.stringify(items),
        embedData: JSON.stringify(embedData),
        redirect_url: window.location.origin + "/order-success",
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    try {
        const response = await fetch('/api/payment/createCODOrder', {
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams(requestData)
        });

        const data = await response.json();

        if (response.ok && data.error === "false") {
            showSuccessToast(data.message || "Đơn hàng COD được tạo thành công!");
            setTimeout(() => {
                window.location.href = data.redirectUrl || "/order-success";
            }, 2000);
        } else {
            showErrorToast(data.message || "Có lỗi xảy ra, vui lòng thử lại.");
            submitButton.disabled = false; // Re-enable on failure
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        showErrorToast("Lỗi kết nối đến máy chủ, vui lòng thử lại.");
        submitButton.disabled = false; // Re-enable on error
    }
});

// Xử lý submit form ZaloPay
document.getElementById("zalopayForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = document.getElementById("createPaymentBtn");
    submitButton.disabled = true;

    // Lấy dữ liệu từ form
    const totalPrice = parseFloat(document.getElementById('totalPriceInputZaloPay').value);
    const rows = document.querySelectorAll('#itemsTable tbody tr');
    const items = [];

    rows.forEach((row) => {
        const itemIdElem = row.querySelector('.itemid');
        const nameElem = row.querySelector('td:nth-child(2) span');
        const quantityElem = row.querySelector('td:nth-child(3) span');
        const unitPriceElem = row.querySelector('td:nth-child(4) span');

        if (itemIdElem && nameElem && quantityElem && unitPriceElem) {
            const item = {
                itemid: parseInt(itemIdElem.textContent.trim(), 10),
                itename: nameElem.textContent.trim(),
                itemprice: parseInt(unitPriceElem.textContent.trim(), 10) * 1000,
                itemquantity: parseInt(quantityElem.textContent.trim(), 10),
            };
            items.push(item);
        }
    });

    const voucherCode = document.getElementById('voucherCodeInputZaloPay').value;
    const usedPoints = document.getElementById('usedPointsHiddenInputZaloPay').value;
    const shippingCost = parseFloat(document.getElementById('shippingCost').textContent);
    const distanceKm = parseFloat(document.getElementById('distanceKm').textContent);
    const estimatedTime = document.getElementById('estimatedTime').textContent;

    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
        shippingCost: (shippingCost * 1000),
        estimatedTime: estimatedTime,
        distanceKm: distanceKm.toFixed(2),
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    const requestData = {
        app_user: document.querySelector('input[name="app_user"]').value,
        totalPrice: totalPrice.toFixed(2),
        items: JSON.stringify(items),
        embedData: JSON.stringify(embedData),
        redirect_url: window.location.origin + "/order-success",
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    try {
        const response = await fetch('/api/payment/createPayment', {
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams(requestData)
        });

        const data = await response.json();

        if (response.ok && data.error === "false") {
            if (data.order_url) {
                showSuccessToast(data.message || "Đang chuyển hướng đến ZaloPay...");
                setTimeout(() => {
                    window.location.href = data.order_url;
                }, 2000);
            } else {
                showErrorToast("Không nhận được URL thanh toán từ ZaloPay.");
                submitButton.disabled = false; // Re-enable on failure
            }
        } else {
            showErrorToast(data.message || "Có lỗi xảy ra khi tạo giao dịch thanh toán.");
            submitButton.disabled = false; // Re-enable on failure
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        showErrorToast("Lỗi kết nối đến máy chủ, vui lòng thử lại.");
        submitButton.disabled = false; // Re-enable on error
    }
});

// Xử lý submit form VNPay
document.getElementById("vnpayForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = document.getElementById("createVNPayBtn");
    submitButton.disabled = true;

    const totalPrice = parseFloat(document.getElementById('totalPriceInputVNPay').value);
    const rows = document.querySelectorAll('#itemsTable tbody tr');
    const items = [];

    rows.forEach((row) => {
        const itemIdElem = row.querySelector('.itemid');
        const nameElem = row.querySelector('td:nth-child(2) span');
        const quantityElem = row.querySelector('td:nth-child(3) span');
        const unitPriceElem = row.querySelector('td:nth-child(4) span');

        if (itemIdElem && nameElem && quantityElem && unitPriceElem) {
            const item = {
                itemid: parseInt(itemIdElem.textContent.trim(), 10),
                itename: nameElem.textContent.trim(),
                itemprice: parseInt(unitPriceElem.textContent.trim(), 10) * 1000,
                itemquantity: parseInt(quantityElem.textContent.trim(), 10),
            };
            items.push(item);
        }
    });

    const voucherCode = document.getElementById('voucherCodeInputVNPay').value;
    const usedPoints = document.getElementById('usedPointsHiddenInputVNPay').value;
    const shippingCost = parseFloat(document.getElementById('shippingCost').textContent);
    const distanceKm = parseFloat(document.getElementById('distanceKm').textContent);
    const estimatedTime = document.getElementById('estimatedTime').textContent;

    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
        shippingCost: (shippingCost * 1000),
        estimatedTime: estimatedTime,
        distanceKm: distanceKm.toFixed(2),
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    const requestData = {
        app_user: document.querySelector('input[name="app_user"]').value,
        totalPrice: totalPrice.toFixed(2),
        items: JSON.stringify(items),
        embedData: JSON.stringify(embedData),
        redirect_url: window.location.origin + "/cart/checkout",
        voucherCode: voucherCode,
        usedPoints: usedPoints
    };

    try {
        const response = await fetch('/api/payment/createVNPayPayment', {
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams(requestData)
        });

        const data = await response.json();

        if (response.ok && data.error === "false") {
            if (data.order_url) {
                showSuccessToast(data.message || "Đang chuyển hướng đến VNPay...");
                setTimeout(() => {
                    window.location.href = data.order_url;
                }, 2000);
            } else {
                showErrorToast("Không nhận được URL thanh toán từ VNPay.");
                submitButton.disabled = false; // Re-enable on failure
            }
        } else {
            showErrorToast(data.message || "Có lỗi xảy ra khi tạo giao dịch thanh toán.");
            submitButton.disabled = false; // Re-enable on failure
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        showErrorToast("Lỗi kết nối đến máy chủ, vui lòng thử lại.");
        submitButton.disabled = false; // Re-enable on error
    }
});

document.getElementById('createVNPayBtn').addEventListener('click', function () {
    const button = this;
    if (!button.disabled) {
        document.getElementById('vnpayForm').dispatchEvent(new Event('submit'));
    }
});

document.getElementById('createCODBtn').addEventListener('click', function () {
    const button = this;
    if (!button.disabled) {
        document.getElementById('codForm').dispatchEvent(new Event('submit'));
    }
});

document.getElementById('createPaymentBtn').addEventListener('click', function () {
    const button = this;
    if (!button.disabled) {
        document.getElementById('zalopayForm').dispatchEvent(new Event('submit'));
    }
});



let originalTotal = parseFloat(document.getElementById('orderTotal').textContent.replace(/[^\d.-]/g, ''));
let shippingcost = parseFloat(document.getElementById('shippingCost').textContent.replace(/[^\d.-]/g, ''));
let voucherDiscount = 0;
let pointsDiscount = 0;
let voucherApplied = false;
let pointsApplied = false;
let lastVoucherCode = '';

document.getElementById('voucherCode').addEventListener('input', function() {
    voucherApplied = false;
    voucherDiscount = 0;
    document.getElementById('discountRow').style.display = 'none';
    document.getElementById("voucherMessage").innerText = "";
    updateOrderTotal();
});

document.getElementById('pointsToUse').addEventListener('input', function() {
    pointsApplied = false;
    pointsDiscount = 0;
    document.getElementById('pointsDiscountRow').style.display = 'none';
    document.getElementById("pointsMessage").innerText = "";
    updateOrderTotal();
});
function updateOrderTotal() {
    const newTotal = originalTotal - voucherDiscount - pointsDiscount;
    document.getElementById('orderTotal').textContent = formatVND(newTotal);
    document.getElementById("totalPriceInput").value = newTotal.toFixed(2);
    document.getElementById("totalPriceInputZaloPay").value = newTotal.toFixed(2);
    document.getElementById("totalPriceInputVNPay").value = newTotal.toFixed(2);
}

function applyVoucher() {
    const code = document.getElementById("voucherCode").value.trim();
    const voucherMessage = document.getElementById("voucherMessage");
    voucherMessage.innerText = "";

    if (!code) {
        voucherMessage.innerText = "Vui lòng nhập mã giảm giá.";
        voucherMessage.style.color = "red";
        return;
    }

    // Kiểm tra mã trùng và đã áp dụng
    if (voucherApplied && code === lastVoucherCode) {
        voucherMessage.innerText = "Mã voucher này đã được áp dụng!";
        voucherMessage.style.color = "red";
        return;
    }

    // Tính tổng tiền gốc (đã trừ điểm nếu có)
    const totalPrice = originalTotal - shippingcost;


    fetch(`/api/payment/check-voucher?code=${encodeURIComponent(code)}&totalPrice=${totalPrice}`, {
        method: "POST"
    })
        .then(response => response.json())
        .then(data => {
            if (data.message.includes("hợp lệ")) {
                // Cập nhật trạng thái
                voucherApplied = true;
                lastVoucherCode = code;
                voucherDiscount = data.discountAmount;

                // Hiển thị thông tin
                document.getElementById("discountRow").style.display = "table-row";
                document.getElementById("discountAmount").textContent = `-${formatVND(data.discountAmount)}`;

                // Cập nhật hiển thị tổng tiền (chỉ giao diện)
                const newTotal = originalTotal - voucherDiscount - pointsDiscount;
                document.getElementById('orderTotal').textContent = formatVND(newTotal);

                // Chỉ cập nhật thông tin mã voucher đã sử dụng
                document.getElementById("voucherCodeInput").value = code;
                document.getElementById("voucherCodeInputZaloPay").value = code;
                document.getElementById("voucherCodeInputVNPay").value = code;

                document.getElementById('totalPriceInput').value = totalPrice.toFixed(2);
                document.getElementById('totalPriceInputZaloPay').value = totalPrice.toFixed(2);
                document.getElementById("totalPriceInputVNPay").value = totalPrice.toFixed(2);

                voucherMessage.innerText = "Áp dụng voucher thành công!";
                voucherMessage.style.color = "green";

                // Cập nhật embed data nếu cần
                const embed = {
                    address: document.getElementById("address").value,
                    phone: document.getElementById("phone").value,
                    note: document.getElementById("note").value
                };
                ["embedDataInput", "embedDataInputCOD"].forEach(id => {
                    document.getElementById(id).value = JSON.stringify(embed);
                });
            } else {
                voucherMessage.innerText = data.message;
                voucherMessage.style.color = "red";
                document.getElementById('totalPriceInput').value = totalPrice.toFixed(2);
                document.getElementById('totalPriceInputZaloPay').value = totalPrice.toFixed(2);
            }
        })
        .catch(error => {
            console.error("Lỗi:", error);
            voucherMessage.innerText = "Lỗi khi kiểm tra mã giảm giá!";
            voucherMessage.style.color = "red";
        });
}

function applyPoints() {
    const points = parseInt(document.getElementById('pointsToUse').value);
    const email = document.querySelector('input[name="app_user"]').value;
    const pointsMessage = document.getElementById('pointsMessage');

    // Kiểm tra đầu vào
    if (isNaN(points) || points <= 0) {
        pointsMessage.innerText = "Vui lòng nhập số điểm hợp lệ.";
        return;
    }

    // Ngăn chặn áp dụng trùng lặp
    if (pointsApplied && points === parseInt(document.getElementById('usedPointsHiddenInput').value)) {
        pointsMessage.innerText = "Điểm này đã được áp dụng trước đó.";
        return;
    }

    const redeemAmount = (points / 1000) * 2000;

    fetch(`/api/payment/redeem?email=${email}&redeemAmountInVND=${redeemAmount}`, { method: 'POST' })
        .then(res => res.json().then(data => ({ ok: res.ok, data })))
        .then(({ ok, data }) => {
            if (!ok || isNaN(data.redeemAmountVND) || isNaN(data.usedPoints)) {
                pointsMessage.innerText = data.message || "Dữ liệu không hợp lệ";
                return;
            }

            // Cập nhật trạng thái hiển thị
            pointsApplied = true;
            pointsDiscount = data.redeemAmountVND;

            // Hiển thị thông tin giảm giá
            document.getElementById('pointsDiscountRow').style.display = 'table-row';
            document.getElementById('pointsDiscountAmount').textContent = `-${formatVND(data.redeemAmountVND)}`;

            // Cập nhật hiển thị tổng tiền (chỉ giao diện)
            const newTotal = originalTotal - voucherDiscount - pointsDiscount;
            document.getElementById('orderTotal').textContent = formatVND(newTotal);


            // Chỉ cập nhật thông tin điểm đã sử dụng
            document.getElementById('usedPointsHiddenInput').value = data.usedPoints;
            document.getElementById('usedPointsHiddenInputZaloPay').value = data.usedPoints;
            document.getElementById('usedPointsHiddenInputVNPay').value = data.usedPoints; // VNPay

            pointsMessage.innerText = `Đã dùng ${data.usedPoints} điểm (Giảm ${formatVND(data.redeemAmountVND)})`;
            pointsMessage.style.color = "green";

            // Cập nhật embed data nếu cần
            const embed = {
                address: document.getElementById("address").value,
                phone: document.getElementById("phone").value,
                note: document.getElementById("note").value
            };
            ["embedDataInput", "embedDataInputCOD"].forEach(id => {
                document.getElementById(id).value = JSON.stringify(embed);
            });
        })
        .catch(err => {
            console.error("Lỗi hệ thống:", err);
            pointsMessage.innerText = "Lỗi hệ thống!";
            pointsMessage.style.color = "red";
        });
}

function validateCheckout() {
    const address = document.getElementById('address').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const isFormValid = address !== '' && phone !== '' && phone.length === 10;
    const codButton = document.getElementById('createCODBtn');
    const zaloPayButton = document.getElementById('createPaymentBtn');
    const vnpayButton = document.getElementById('createVNPayBtn');

    if (isFormValid) {
        codButton.disabled = false;
        zaloPayButton.disabled = false;
        vnpayButton.disabled = false;
    } else {
        codButton.disabled = true;
        zaloPayButton.disabled = true;
        vnpayButton.disabled = true;
    }
}

document.getElementById('address').addEventListener('input', validateCheckout);
document.getElementById('phone').addEventListener('input', validateCheckout);

window.onload = function () {
    validateCheckout();
};

document.addEventListener('DOMContentLoaded', function () {
    const pointsInput = document.getElementById('pointsToUse');
    const usedPointsInput = document.getElementById('usedPointsInput');
    const decreaseBtn = document.querySelector('.points-control .minus-btn');
    const increaseBtn = document.querySelector('.points-control .plus-btn');
    const step = 1000;
    const maxPoints = 10000;

    pointsInput.value = '0';

    decreaseBtn.addEventListener('click', function () {
        let currentValue = parseInt(pointsInput.value) || 0;
        currentValue = Math.max(0, currentValue - step);
        updatePoints(currentValue);
    });

    increaseBtn.addEventListener('click', function () {
        let currentValue = parseInt(pointsInput.value) || 0;
        currentValue += step;
        if (currentValue <= maxPoints) {
            updatePoints(currentValue);
        } else {
            pointsInput.value = maxPoints;
            usedPointsInput.value = maxPoints;
        }
    });

    function updatePoints(value) {
        pointsInput.value = value;
        usedPointsInput.value = value;
    }
});
function showErrorToast(message) {
    const toastContainer = document.querySelector('.toast-container');
    const toastElement = document.createElement('div');
    toastElement.className = 'toast align-items-center text-dark bg-warning border-0 shadow-lg fs-6 position-fixed top-50 start-50 translate-middle z-3';
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');
    toastElement.setAttribute('style', 'background-color: #ffebd2 !important; min-width: 300px;');


    toastElement.innerHTML = `
        <div class="d-flex">
            <div class="toast-body fw-semibold">
                ⚠️ ${message}
            </div>
            <button type="button" class="btn-close btn-close-dark me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toastElement);

    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 50000
    });
    toast.show();

    // Xóa toast khỏi DOM sau khi ẩn
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}
function showSuccessToast(message) {
    const toastContainer = document.querySelector('.toast-container');
    const toastElement = document.createElement('div');
    toastElement.className = 'toast align-items-center text-white bg-success border-0 shadow-lg fs-6';
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');
    toastElement.style.minWidth = '300px';
    toastElement.style.backgroundColor = '#82d556';

    toastElement.innerHTML = `
        <div class="d-flex">
            <div class="toast-body fw-semibold">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toastElement);

    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 2000
    });
    toast.show();

    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}


