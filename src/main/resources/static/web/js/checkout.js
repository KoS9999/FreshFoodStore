// Phần xử lý thanh toán ZaloPay
document.getElementById('createPaymentBtn').addEventListener('click', function () {
    // KHÔNG TÍNH LẠI TỔNG TIỀN TỪ ĐẦU
    // Sử dụng trực tiếp giá trị đã được cập nhật từ voucher/điểm
    const totalPrice = parseFloat(document.getElementById('totalPriceInputZaloPay').value);

    // Lấy thông tin sản phẩm (chỉ để gửi dữ liệu, không dùng để tính toán)
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

    // Lấy các thông tin vận chuyển (chỉ để gửi dữ liệu)
    const shippingCost = parseFloat(document.getElementById('shippingCost').textContent);
    const distanceKm = parseFloat(document.getElementById('distanceKm').textContent);
    const estimatedTime = document.getElementById('estimatedTime').textContent;

    // Chuẩn bị dữ liệu nhúng
    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
        shippingCost: (shippingCost * 1000),
        estimatedTime: estimatedTime,
        distanceKm: distanceKm.toFixed(2),
        voucherCode: document.getElementById('voucherCodeInputZaloPay').value,
        usedPoints: document.getElementById('usedPointsHiddenInputZaloPay').value
    };

    // Cập nhật các trường form
    document.getElementById('embedDataInput').value = JSON.stringify(embedData);
    document.getElementById('itemsInput').value = JSON.stringify(items);

    // Debug
    console.log("Total Price (Đã bao gồm phí ship và giảm giá):", totalPrice);
    console.log("Shipping Cost:", shippingCost);

    // Gửi form
    document.getElementById('zalopayForm').submit();
});




document.getElementById("zalopayForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(event.target);
    const requestData = Object.fromEntries(formData.entries());

    try {
        const response = await fetch(event.target.action, {
            method: "POST",
            body: new URLSearchParams(requestData),
        });

        if (response.ok) {
            const data = await response.json();
            if (data.order_url) {
                // Chuyển hướng sang trang thanh toán ZaloPay
                window.location.href = data.order_url;
            } else {
                console.error("Không nhận được order_url từ ZaloPay");
                alert("Không thể tạo giao dịch thanh toán. Vui lòng thử lại.");
            }
        } else {
            const error = await response.text();
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra: " + error);
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        alert("Có lỗi xảy ra khi kết nối đến máy chủ.");
    }
});
document.getElementById('createCODBtn').addEventListener('click', function () {
    // Sử dụng tổng tiền ĐÃ ÁP DỤNG KHUYẾN MÃI từ input ẩn
    const totalPrice = parseFloat(document.getElementById('totalPriceInput').value);

    // Lấy thông tin sản phẩm
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

    // Lấy thông tin khuyến mãi và vận chuyển
    const voucherCode = document.getElementById('voucherCodeInput').value;
    const usedPoints = document.getElementById('usedPointsHiddenInput').value;
    const shippingCost = parseFloat(document.getElementById('shippingCost').textContent);
    const distanceKm = parseFloat(document.getElementById('distanceKm').textContent);
    const estimatedTime = document.getElementById('estimatedTime').textContent;

    // Cập nhật embedData với thông tin khuyến mãi
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

    // Cập nhật các trường form COD
    document.getElementById('embedDataInputCOD').value = JSON.stringify(embedData);
    document.getElementById('itemsInputCOD').value = JSON.stringify(items);
    document.getElementById('totalPriceInput').value = totalPrice.toFixed(2); // Đảm bảo giá trị cuối cùng

    // Debug
    console.log("COD Total Price:", totalPrice);
    console.log("Voucher Code:", voucherCode);
    console.log("Used Points:", usedPoints);

    // Gửi form
    document.getElementById('codForm').submit();
});

document.getElementById("codForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(event.target);
    const requestData = Object.fromEntries(formData.entries());

    try {
        const response = await fetch(event.target.action, {
            method: "POST",
            body: new URLSearchParams(requestData),
        });

        if (response.ok) {
            const message = await response.text();
            alert("Đơn hàng của bạn đã được tạo thành công. Cảm ơn bạn đã sử dụng dịch vụ!");
        } else {
            const error = await response.text();
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra: " + error);
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        alert("Có lỗi xảy ra khi kết nối đến máy chủ.");
    }
});
function validateNumber(input) {
    const phoneError = document.getElementById('phoneError');
    input.value = input.value.replace(/[^0-9]/g, '');
    if (input.value.length !== 10) {
        input.setCustomValidity('Số điện thoại phải có đúng 10 chữ số.');
        phoneError.style.display = 'block';
    } else if (input.value.charAt(0) !== '0') {
        input.setCustomValidity('Số điện thoại phải bắt đầu bằng số 0.');
        phoneError.style.display = 'block';
    } else {
        input.setCustomValidity('');
        phoneError.style.display = 'none';
    }
}

let originalTotal = parseFloat(document.getElementById('orderTotal').textContent.replace(/[^\d.-]/g, ''));
let shippingcost = parseFloat(document.getElementById('shippingCost').textContent.replace(/[^\d.-]/g, ''));
let voucherDiscount = 0;
let pointsDiscount = 0;
let voucherApplied = false;
let pointsApplied = false;
let lastVoucherCode = '';

document.getElementById('voucherCode').addEventListener('input', function() {
    // Reset khi người dùng thay đổi mã voucher
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
}

function applyVoucher() {
    const code = document.getElementById("voucherCode").value.trim();
    const voucherMessage = document.getElementById("voucherMessage");
    voucherMessage.innerText = "";

    // Kiểm tra mã trùng và đã áp dụng
    if (voucherApplied && code === lastVoucherCode) {
        voucherMessage.innerText = "Mã voucher này đã được áp dụng!";
        voucherMessage.style.color = "red";
        return;
    }

    // Tính tổng tiền gốc (đã trừ điểm nếu có)
    const totalPrice = originalTotal - shippingcost;

    if (!code) {
        voucherMessage.innerText = "Vui lòng nhập mã giảm giá.";
        voucherMessage.style.color = "red";
        return;
    }

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
                document.getElementById("voucherCodeInput").value = code; // Cho COD
                document.getElementById("voucherCodeInputZaloPay").value = code; // Cho ZaloPay

                document.getElementById('totalPriceInput').value = totalPrice.toFixed(2);
                document.getElementById('totalPriceInputZaloPay').value = totalPrice.toFixed(2);

                // KHÔNG cập nhật totalPriceInput/totalPriceInputZaloPay
                // => Để server tính toán giảm giá từ voucher

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
            }
        })
        .catch(error => {
            console.error("Lỗi:", error);
            voucherMessage.innerText = "Lỗi khi kiểm tra mã giảm giá!";
            voucherMessage.style.color = "red";
        });
}

let isPointsApplied = false;
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

            // KHÔNG cập nhật totalPriceInput/totalPriceInputZaloPay
            // => Để server tính toán giảm giá từ điểm

            // Chỉ cập nhật thông tin điểm đã sử dụng
            document.getElementById('usedPointsHiddenInput').value = data.usedPoints;
            document.getElementById('usedPointsHiddenInputZaloPay').value = data.usedPoints;

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
